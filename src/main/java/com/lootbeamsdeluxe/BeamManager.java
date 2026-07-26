/*
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;

/**
 * Owns the live beams (one per tile) and any pending fanfares.
 * Everything in here has to run on the client thread.
 */
class BeamManager
{
	// give up and show the beam if the fanfare anims never report finishing
	private static final int FANFARE_TIMEOUT_TICKS = 8;

	@AllArgsConstructor
	private static class FanfareState
	{
		DeluxeBeam effect;
		Resolution resolution;
		Fanfare fanfare;
		int startTick;
	}

	private final Client client;
	private final ClientThread clientThread;

	private final Map<WorldPoint, DeluxeBeam> beams = new HashMap<>();
	private final Map<WorldPoint, FanfareState> pendingFanfares = new HashMap<>();

	BeamManager(Client client, ClientThread clientThread)
	{
		this.client = client;
		this.clientThread = clientThread;
	}

	void handleTile(WorldPoint worldPoint, Resolution resolution, @Nullable Fanfare fanfare)
	{
		if (resolution.isBeamless())
		{
			cancelFanfare(worldPoint);
			removeBeam(worldPoint);
			return;
		}

		FanfareState pending = pendingFanfares.get(worldPoint);
		if (pending != null)
		{
			// don't restart the fanfare, just change what the beam turns into
			pending.resolution = resolution;
			return;
		}

		DeluxeBeam existing = beams.get(worldPoint);
		if (existing != null)
		{
			existing.setRecipe(resolution.getStyle().getRecipe());
			existing.setColors(resolution.getPrimary(), resolution.getSecondary());
			return;
		}

		if (fanfare != null)
		{
			startFanfare(worldPoint, resolution, fanfare);
		}
		else
		{
			spawnBeam(worldPoint, resolution);
		}
	}

	// warning stage (when the style has one), then the main effect, then the beam
	private void startFanfare(WorldPoint worldPoint, Resolution resolution, Fanfare fanfare)
	{
		FanfareState state = new FanfareState(null, resolution, fanfare, client.getTickCount());
		pendingFanfares.put(worldPoint, state);

		BeamRecipe warning = fanfare.getStyle().getWarningRecipe();
		if (warning == null)
		{
			playMainStage(worldPoint);
			return;
		}

		Color flash = lighten(fanfare.getSecondary());
		state.effect = new DeluxeBeam(client, clientThread, worldPoint,
			warning, flash, flash, () -> playMainStage(worldPoint));
	}

	// halfway to white, so the tinted warning still reads as a flash
	static Color lighten(Color color)
	{
		return new Color(
			(color.getRed() + 255) / 2,
			(color.getGreen() + 255) / 2,
			(color.getBlue() + 255) / 2);
	}

	private void playMainStage(WorldPoint worldPoint)
	{
		FanfareState state = pendingFanfares.get(worldPoint);
		if (state == null)
		{
			return;
		}

		if (state.effect != null)
		{
			state.effect.remove();
		}
		Fanfare fanfare = state.fanfare;
		state.effect = new DeluxeBeam(client, clientThread, worldPoint,
			fanfare.getStyle().getRecipe(), fanfare.getPrimary(), fanfare.getSecondary(),
			() -> finishFanfare(worldPoint));

		// Electric ramps up slowly and looks stalled if it only starts after the
		// fanfare — let it ramp up underneath instead
		if (state.resolution.getStyle() == BeamStyle.ELECTRIC)
		{
			spawnBeam(worldPoint, state.resolution);
		}
	}

	private void finishFanfare(WorldPoint worldPoint)
	{
		FanfareState state = pendingFanfares.remove(worldPoint);
		if (state == null)
		{
			return;
		}

		state.effect.remove();

		DeluxeBeam existing = beams.get(worldPoint);
		if (existing != null)
		{
			// spawned early during the fanfare — retarget in case the tile changed since
			existing.setRecipe(state.resolution.getStyle().getRecipe());
			existing.setColors(state.resolution.getPrimary(), state.resolution.getSecondary());
		}
		else
		{
			spawnBeam(worldPoint, state.resolution);
		}
	}

	private void cancelFanfare(WorldPoint worldPoint)
	{
		FanfareState state = pendingFanfares.remove(worldPoint);
		if (state != null)
		{
			state.effect.remove();
		}
	}

	private void spawnBeam(WorldPoint worldPoint, Resolution resolution)
	{
		DeluxeBeam beam = new DeluxeBeam(client, clientThread, worldPoint,
			resolution.getStyle().getRecipe(), resolution.getPrimary(), resolution.getSecondary(), null);
		beams.put(worldPoint, beam);
	}

	private void removeBeam(WorldPoint worldPoint)
	{
		DeluxeBeam beam = beams.remove(worldPoint);
		if (beam != null)
		{
			beam.remove();
		}
	}

	// safety net for fanfares whose anim never reported finishing
	void tick()
	{
		int now = client.getTickCount();
		for (WorldPoint worldPoint : new ArrayList<>(pendingFanfares.keySet()))
		{
			FanfareState state = pendingFanfares.get(worldPoint);
			if (state != null && now - state.startTick >= FANFARE_TIMEOUT_TICKS)
			{
				finishFanfare(worldPoint);
			}
		}
	}

	void removeWorldView(int worldViewId)
	{
		beams.values().removeIf(beam ->
		{
			if (beam.getWorldView() == worldViewId)
			{
				beam.remove();
				return true;
			}
			return false;
		});
		pendingFanfares.values().removeIf(state ->
		{
			if (state.effect.getWorldView() == worldViewId)
			{
				state.effect.remove();
				return true;
			}
			return false;
		});
	}

	void removeAll()
	{
		for (DeluxeBeam beam : beams.values())
		{
			beam.remove();
		}
		beams.clear();

		for (FanfareState state : pendingFanfares.values())
		{
			state.effect.remove();
		}
		pendingFanfares.clear();
	}
}
