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
	@Nullable
	private RuneLiteObject overlay;

	private BeamRecipe recipe;
	private Color primary;
	private Color secondary;
	private boolean removed;

	DeluxeBeam(Client client, ClientThread clientThread, WorldPoint worldPoint,
		BeamRecipe recipe, Color primary, Color secondary, @Nullable Runnable onAnimationFinished)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.recipe = recipe;
		this.primary = primary;
		this.secondary = secondary;
		this.onAnimationFinished = onAnimationFinished;
		runeLiteObject = client.createRuneLiteObject();
		localPoint = LocalPoint.fromWorld(client, worldPoint);
		plane = client.getPlane();

		update();

		runeLiteObject.setLocation(localPoint, plane);
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

			AnimationController animationController = new AnimationController(client, recipe.getAnimId());
			if (recipe.isLoop())
			{
				animationController.setOnFinished(AnimationController::loop);
			}
			else
			{
				// invokeLater so the handoff doesn't mutate the scene's object
				// list while the client is mid-way through ticking it
				animationController.setOnFinished(ac ->
				{
					if (onAnimationFinished != null)
					{
						clientThread.invokeLater(onAnimationFinished);
					}
				});
			}

			runeLiteObject.setAnimationController(animationController);
			runeLiteObject.setModel(model);
			updateOverlay();
			return true;
		});
	}

	// client thread only; runs after the main object has its model
	private void updateOverlay()
	{
		if (!recipe.isHalfCycleOverlay())
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

		AnimationController animationController = new AnimationController(client, recipe.getAnimId());
		animationController.setOnFinished(AnimationController::loop);
		Animation animation = animationController.getAnimation();
		if (animation != null)
		{
			animationController.setFrame(animation.getNumFrames() / 2);
		}

		if (overlay == null)
		{
			overlay = client.createRuneLiteObject();
			overlay.setLocation(localPoint, plane);
			overlay.setActive(true);
		}
		overlay.setAnimationController(animationController);
		overlay.setModel(model);
	}

	@Nullable
	private Model buildModel()
	{
		ModelData md = client.loadModelData(recipe.getModelId());
		if (md == null)
		{
			return null;
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

		if (recipe.getTransparency() >= 0 || hiddenFaces != null)
		{
			md.cloneTransparencies(true);
			byte[] transparencies = md.getFaceTransparencies();
			if (transparencies != null)
			{
				if (recipe.getTransparency() >= 0)
				{
					Arrays.fill(transparencies, (byte) recipe.getTransparency());
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
