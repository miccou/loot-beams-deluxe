/*
 * Copyright (c) 2021, Trevor <https://github.com/Trevor159>
 * Copyright (c) 2026, Michael Cousins <https://github.com/miccou>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.lootbeamsdeluxe;

import java.awt.Color;
import java.util.Arrays;
import javax.annotation.Nullable;
import net.runelite.api.Animation;
import net.runelite.api.AnimationController;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.JagexColor;
import net.runelite.api.Model;
import net.runelite.api.ModelData;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;

/**
 * One effect on a tile, built from a {@link BeamRecipe} plus the two user
 * colours. Started life as Ground Items' Lootbeam, generalised to arbitrary
 * recipes with independent primary/secondary recolours.
 */
class DeluxeBeam
{
	private final Client client;
	private final ClientThread clientThread;
	private final RuneLiteObject runeLiteObject;
	private final LocalPoint localPoint;
	private final int plane;

	@Nullable
	private final Runnable onAnimationFinished;

	// second copy of the model, only when the recipe wants a half-cycle overlay
	// or overlapping passes
	@Nullable
	private RuneLiteObject overlay;

	private BeamRecipe recipe;
	private Color primary;
	private Color secondary;
	private boolean removed;
	private boolean finishReported;

	// bumped on every rebuild so frame watchers from the old build give up
	private int generation;

	DeluxeBeam(Client client, ClientThread clientThread, WorldPoint worldPoint,
		BeamRecipe recipe, Color primary, Color secondary, @Nullable Runnable onAnimationFinished)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.recipe = recipe;
		this.primary = primary;
		this.secondary = secondary;
		this.onAnimationFinished = onAnimationFinished;
		runeLiteObject = new ResizableObject();
		localPoint = LocalPoint.fromWorld(client, worldPoint);
		plane = client.getPlane();

		runeLiteObject.setLocation(localPoint, plane);
		update();
		runeLiteObject.setActive(true);
	}

	int getWorldView()
	{
		return runeLiteObject.getWorldView();
	}

	void setColors(Color primary, Color secondary)
	{
		if (this.primary.equals(primary) && this.secondary.equals(secondary))
		{
			return;
		}

		this.primary = primary;
		this.secondary = secondary;
		update();
	}

	void setRecipe(BeamRecipe recipe)
	{
		if (this.recipe.equals(recipe))
		{
			return;
		}

		this.recipe = recipe;
		update();
	}

	private void update()
	{
		clientThread.invoke(() ->
		{
			// the model can take a tick to load — don't revive a beam that was
			// removed in the meantime
			if (removed)
			{
				return true;
			}

			Model model = buildModel();
			if (model == null)
			{
				return false;
			}

			generation++;
			finishReported = false;
			boolean overlapping = recipe.getLoopOverlapMs() > 0;

			AnimationController animationController = newAnimationController();
			if (overlapping)
			{
				animationController.setOnFinished(ac -> endPass(runeLiteObject, ac));
			}
			else if (recipe.isLoop())
			{
				animationController.setOnFinished(this::restartLoop);
			}
			else
			{
				animationController.setOnFinished(ac -> reportFinished());
				if (recipe.getEndPercent() < 100)
				{
					whenFrameReached(animationController, frameAt(animationController, recipe.getEndPercent()),
						this::reportFinished);
				}
			}

			seek(animationController, recipe.getStartPercent());
			armSkip(animationController);
			runeLiteObject.setAnimationController(animationController);
			runeLiteObject.setModel(model);
			// a rebuild can land while the main copy is hidden between overlapping passes
			runeLiteObject.setActive(true);
			updateOverlay();

			if (overlapping && overlay != null)
			{
				handOverNearEnd(animationController, overlay, runeLiteObject);
			}
			return true;
		});
	}

	// client thread only; runs after the main object has its model
	private void updateOverlay()
	{
		boolean overlapping = recipe.getLoopOverlapMs() > 0;
		if (!recipe.isHalfCycleOverlay() && !overlapping)
		{
			if (overlay != null)
			{
				overlay.setActive(false);
				overlay = null;
			}
			return;
		}

		Model model = buildModel();
		if (model == null)
		{
			return;
		}

		if (overlay == null)
		{
			overlay = new ResizableObject();
			overlay.setLocation(localPoint, plane);
		}
		overlay.setModel(model);

		if (overlapping)
		{
			// sits hidden until the main copy hands over to it
			overlay.setActive(false);
			return;
		}

		AnimationController animationController = newAnimationController();
		animationController.setOnFinished(this::restartLoop);
		seek(animationController, recipe.getStartPercent() + 50);
		armSkip(animationController);
		overlay.setAnimationController(animationController);
		overlay.setActive(true);
	}

	private void restartLoop(AnimationController animationController)
	{
		animationController.loop();
		seek(animationController, recipe.getLoopPercent());
		armSkip(animationController);
	}

	private void armSkip(AnimationController animationController)
	{
		if (recipe.getSkipToPercent() <= 0)
		{
			return;
		}

		int skipTo = frameAt(animationController, recipe.getSkipToPercent());
		whenFrameReached(animationController, frameAt(animationController, recipe.getSkipFromPercent()), () ->
		{
			// a pass that started past the skip shouldn't get pulled back to it
			if (animationController.getFrame() < skipTo)
			{
				animationController.setFrame(skipTo);
			}
		});
	}

	private void startPass(RuneLiteObject copy, RuneLiteObject other)
	{
		AnimationController animationController = newAnimationController();
		animationController.setOnFinished(ac -> endPass(copy, ac));
		seek(animationController, recipe.getLoopPercent());
		armSkip(animationController);
		copy.setAnimationController(animationController);
		copy.setActive(true);
		handOverNearEnd(animationController, other, copy);
	}

	// hold the last frame until the copy is hidden, rather than snapping back
	// to the model's rest pose for a frame
	private void endPass(RuneLiteObject copy, AnimationController animationController)
	{
		animationController.setFrame(animationController.getAnimation().getNumFrames() - 1);
		clientThread.invokeLater(() ->
		{
			if (copy.getAnimationController() == animationController)
			{
				copy.setActive(false);
			}
		});
	}

	private void handOverNearEnd(AnimationController animationController, RuneLiteObject next, RuneLiteObject current)
	{
		Animation animation = animationController.getAnimation();
		int[] frameLengths = animation == null ? null : animation.getFrameLengths();
		if (frameLengths == null)
		{
			return;
		}

		// the last frame that still has loopOverlapMs of anim left from its start
		int handoffFrame = 0;
		int remaining = 0;
		for (int frame = frameLengths.length - 1; frame > 0; frame--)
		{
			remaining += frameLengths[frame] * Constants.CLIENT_TICK_LENGTH * 100 / recipe.getSpeedPercent();
			if (remaining >= recipe.getLoopOverlapMs())
			{
				handoffFrame = frame;
				break;
			}
		}

		whenFrameReached(animationController, handoffFrame, () -> startPass(next, current));
	}

	// controllers only report finishing, so anything mid-anim has to poll. Gives
	// up if the effect is rebuilt or removed first
	private void whenFrameReached(AnimationController animationController, int frame, Runnable action)
	{
		int watching = generation;
		clientThread.invokeLater(() ->
		{
			if (removed || generation != watching)
			{
				return true;
			}

			if (animationController.getFrame() < frame)
			{
				return false;
			}

			action.run();
			return true;
		});
	}

	// invokeLater so the handoff doesn't mutate the scene's object list while
	// the client is mid-way through ticking it
	private void reportFinished()
	{
		if (finishReported || onAnimationFinished == null)
		{
			return;
		}

		finishReported = true;
		clientThread.invokeLater(onAnimationFinished);
	}

	private static int frameAt(AnimationController animationController, int percent)
	{
		Animation animation = animationController.getAnimation();
		return animation == null ? 0 : animation.getNumFrames() * percent / 100;
	}

	private static void seek(AnimationController animationController, int percent)
	{
		if (percent <= 0)
		{
			return;
		}

		Animation animation = animationController.getAnimation();
		if (animation == null)
		{
			return;
		}

		int numFrames = animation.getNumFrames();
		if (numFrames > 0)
		{
			animationController.setFrame(numFrames * percent / 100 % numFrames);
		}
	}

	private AnimationController newAnimationController()
	{
		if (recipe.getSpeedPercent() == 100)
		{
			return new AnimationController(client, recipe.getAnimId());
		}
		return new SpedUpAnimationController(client, recipe.getAnimId(), recipe.getSpeedPercent());
	}

	// applies the recipe's resize to each frame after the anim has posed it
	private class ResizableObject extends RuneLiteObject
	{
		ResizableObject()
		{
			super(client);
		}

		@Override
		public Model getModel()
		{
			Model model = super.getModel();
			int resize = recipe.getResize();
			// with no anim playing this is the base model itself, which would
			// shrink a little more every frame if scaled in place
			if (resize == 128 || model == null || model == getBaseModel())
			{
				return model;
			}
			return model.scale(resize, resize, resize);
		}
	}

	// the object ticks its controller with however many client ticks have
	// passed, so scaling that count changes the playback speed
	private static class SpedUpAnimationController extends AnimationController
	{
		private final int speedPercent;
		private int leftover;

		SpedUpAnimationController(Client client, int animId, int speedPercent)
		{
			super(client, animId);
			this.speedPercent = speedPercent;
		}

		@Override
		public void tick(int ticks)
		{
			int scaled = ticks * speedPercent + leftover;
			leftover = scaled % 100;
			super.tick(scaled / 100);
		}
	}

	@Nullable
	private Model buildModel()
	{
		ModelData md = client.loadModelData(recipe.getModelId());
		if (md == null)
		{
			return null;
		}

		int[] extraModelIds = recipe.getExtraModelIds();
		if (extraModelIds.length > 0)
		{
			ModelData[] parts = new ModelData[extraModelIds.length + 1];
			parts[0] = md;
			for (int i = 0; i < extraModelIds.length; i++)
			{
				parts[i + 1] = client.loadModelData(extraModelIds[i]);
				if (parts[i + 1] == null)
				{
					return null;
				}
			}
			md = client.mergeModels(parts);
		}

		md = md.cloneColors();

		// note which faces to hide before the recolours overwrite the face colours
		boolean[] hiddenFaces = null;
		short[] hideFinds = recipe.getHideFinds();
		short[] originalColors = md.getFaceColors();
		if (hideFinds.length > 0 && originalColors != null)
		{
			hiddenFaces = new boolean[originalColors.length];
			for (int i = 0; i < originalColors.length; i++)
			{
				for (short hide : hideFinds)
				{
					if (originalColors[i] == hide)
					{
						hiddenFaces[i] = true;
					}
				}
			}
		}

		short[] bakedFinds = recipe.getBakedFinds();
		short[] bakedReplaces = recipe.getBakedReplaces();
		for (int i = 0; i < bakedFinds.length; i++)
		{
			md.recolor(bakedFinds[i], bakedReplaces[i]);
		}

		short primaryHsl = saturate(JagexColor.rgbToHSL(primary.getRGB(), 1.0d), recipe.getPrimarySaturationAdd());
		for (short find : recipe.getPrimaryFinds())
		{
			md.recolor(find, primaryHsl);
		}

		short secondaryHsl = saturate(JagexColor.rgbToHSL(secondary.getRGB(), 1.0d), recipe.getSecondarySaturationAdd());
		for (short find : recipe.getSecondaryFinds())
		{
			md.recolor(find, secondaryHsl);
		}

		if (recipe.getTransparency() >= 0 || recipe.getTransparencyAdd() > 0 || hiddenFaces != null)
		{
			md.cloneTransparencies(true);
			byte[] transparencies = md.getFaceTransparencies();
			if (transparencies != null)
			{
				if (recipe.getTransparency() >= 0)
				{
					Arrays.fill(transparencies, (byte) recipe.getTransparency());
				}
				if (recipe.getTransparencyAdd() > 0)
				{
					for (int i = 0; i < transparencies.length; i++)
					{
						transparencies[i] = (byte) Math.min(254, (transparencies[i] & 0xff) + recipe.getTransparencyAdd());
					}
				}
				if (hiddenFaces != null)
				{
					for (int i = 0; i < transparencies.length && i < hiddenFaces.length; i++)
					{
						if (hiddenFaces[i])
						{
							transparencies[i] = (byte) 254;
						}
					}
				}
			}
		}

		if (recipe.hasScale() || recipe.hasTranslate())
		{
			ModelData vertices = md.cloneVertices();
			if (recipe.hasScale())
			{
				vertices.scale(recipe.getScaleX(), recipe.getScaleY(), recipe.getScaleZ());
			}
			if (recipe.hasTranslate())
			{
				vertices.translate(recipe.getTranslateX(), recipe.getTranslateY(), recipe.getTranslateZ());
			}
		}

		return md.light(
			recipe.getAmbientAdd() + ModelData.DEFAULT_AMBIENT,
			recipe.getContrastAdd() + ModelData.DEFAULT_CONTRAST,
			ModelData.DEFAULT_X, ModelData.DEFAULT_Y, ModelData.DEFAULT_Z);
	}

	private static short saturate(short hsl, int saturationAdd)
	{
		if (saturationAdd == 0)
		{
			return hsl;
		}
		int saturation = Math.max(0, Math.min(JagexColor.SATURATION_MAX,
			JagexColor.unpackSaturation(hsl) + saturationAdd));
		return JagexColor.packHSL(JagexColor.unpackHue(hsl), saturation, JagexColor.unpackLuminance(hsl));
	}

	void remove()
	{
		removed = true;
		runeLiteObject.setActive(false);
		if (overlay != null)
		{
			overlay.setActive(false);
		}
	}
}
