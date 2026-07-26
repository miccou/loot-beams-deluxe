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

import javax.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The entrance effect played the moment qualifying loot drops. A style is an
 * optional warning stage (tinted with the secondary colour, lightened into a
 * flash) followed by the main effect (primary/secondary like a beam), then the
 * beam takes over.
 */
@Getter
@RequiredArgsConstructor
public enum FanfareStyle
{
	// the ToA Wardens sequence — warning flash, then the strike, same order as
	// the raid. One model played twice, recoloured per stage. (The anim is
	// actually the Hallowed Sepulchre end-strike, ToA just reuses it)
	LIGHTNING("Lightning strike", lightningRecipe(), lightningRecipe()),

	// ToA Path of Crondis column trap (spotanim 2129) — a circular column that
	// rises out of the ground and sinks back. Single-colour model, so the whole
	// column takes the primary colour (alt anims: 9560 hold, 9561 lower)
	RISING_COLUMN("Rising column", null, BeamRecipe.builder()
		.modelId(46216)
		.animId(9559)
		.primaryFinds(new short[]{20405})
		.ambientAdd(50)
		.contrastAdd(50)
		.loop(false)
		.build()),

	// Combat Achievements tier-reward teleport (spotanim 1950) — a celebratory
	// light column with spinning laurels. Primary drives the column, secondary
	// the ribbon accents; the small gold/green laurel details stay native
	TROPHY_TELEPORT("Trophy teleport", null, BeamRecipe.builder()
		.modelId(42720)
		.animId(9135)
		.primaryFinds(new short[]{-31817, -31813, -31808, -31796, -31784})
		.secondaryFinds(new short[]{912, 926})
		.ambientAdd(30)
		.contrastAdd(12)
		.loop(false)
		.build()),

	// Sins of the Father — Drakan incinerating the Myreque (spotanim 1813). A
	// fiery burst; the flame body takes primary, the bright tips secondary,
	// the little grey ash stays native
	DRAKAN_INCINERATE("Drakan incinerate", null, BeamRecipe.builder()
		.modelId(39815)
		.animId(8746)
		.primaryFinds(new short[]{937, 1984, 1977, 945})
		.secondaryFinds(new short[]{3014})
		.ambientAdd(40)
		.contrastAdd(40)
		.loop(false)
		.build()),
	;

	private final String displayName;

	// plays first when present, as a lightened flash of the secondary colour
	@Nullable
	private final BeamRecipe warningRecipe;

	private final BeamRecipe recipe;

	// spotanim 2197's model with its recolourable face groups (dumped from the
	// cache) driven by the colour pickers instead of the baked ToA red
	private static BeamRecipe lightningRecipe()
	{
		return BeamRecipe.builder()
			.modelId(40177)
			.animId(8680)
			.primaryFinds(new short[]{-32663, -32649, -32533, -32543, -32572, -32560})
			.ambientAdd(60)
			.contrastAdd(60)
			.loop(false)
			.build();
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
