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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.AnimationID;

@Getter
@RequiredArgsConstructor
public enum BeamStyle
{
	// the vanilla "Light" beam — same model (5809) and recolour target (6371)
	// as Ground Items. One palette for the whole model, so only primary applies
	LIGHT("Light", BeamRecipe.builder()
		.modelId(5809)
		.animId(AnimationID.ENAKH_LIGHT_STREAMING)
		.primaryFinds(new short[]{6371})
		.build()),

	// the vanilla "Modern" beam. Two recolourable groups (26432 core, 26584
	// lattice) that the built-in beam drives from one colour — here they're
	// independent. The lattice's lighting washes it out, hence the saturation bump
	MODERN("Modern", BeamRecipe.builder()
		.modelId(43330)
		.animId(AnimationID.FX_BEAM_IDLE)
		.primaryFinds(new short[]{26432})
		.secondaryFinds(new short[]{26584})
		.secondarySaturationAdd(2)
		.ambientAdd(75)
		.contrastAdd(1875)
		.build()),

	// the Wine of Zamorak zap (spotanim 78) — the flames you get for stealing
	// his wine. Primary is the main glow, secondary the bands running through
	// it. The burst fades as its loop restarts, so a second copy plays half a
	// cycle behind to keep the flame full. Ambient/contrast match the spotanim's
	// own lighting. The model sits a quarter tile west of its origin, so it's
	// shifted back over the tile centre
	FIRE("Fire", BeamRecipe.builder()
		.modelId(2267)
		.animId(AnimationID.FLAMEWALL)
		.primaryFinds(new short[]{8128, 7104})
		.primarySaturationAdd(2)
		.secondaryFinds(new short[]{5056, 4019, 2987})
		.secondarySaturationAdd(2)
		.translateX(32)
		.transparency(130)
		.ambientAdd(75)
		.contrastAdd(75)
		.halfCycleOverlay(true)
		.build()),

	// Grotesque Guardians dusk lightning (spotanim 1416). Primary is the broad
	// flash, secondary the bolts arcing through it. The raw asset covers a huge
	// area, so it's squeezed horizontally to fit on one tile
	ELECTRIC("Electric", BeamRecipe.builder()
		.modelId(34180)
		.animId(AnimationID.GG_PROJECTILE_LIGHTNING_01)
		.primaryFinds(new short[]{3005, 6077, 8128})
		.secondaryFinds(new short[]{960})
		.scaleX(64)
		.scaleZ(64)
		.startPercent(40)
		.build()),

	// long-looping mist (spotanim 2541) — the model comes translucent out of the cache
	CLOUD("Cloud", BeamRecipe.builder()
		.modelId(4086)
		.animId(10407)
		.primaryFinds(new short[]{10543})
		.ambientAdd(30)
		.contrastAdd(30)
		.startPercent(30)
		.build()),

	// smoke devil cloud (spotanim 642), a slow rolling grey plume. Primary is
	// the body, secondary the brighter wisps on top. Second copy half a cycle
	// behind fills the dip where the loop restarts
	SMOKE("Smoke", BeamRecipe.builder()
		.modelId(28443)
		.animId(3857)
		.primaryFinds(new short[]{9562})
		.secondaryFinds(new short[]{11491})
		.ambientAdd(30)
		.contrastAdd(30)
		.halfCycleOverlay(true)
		.build()),

	// Abyssal Sire miasma (spotanim 1275), a long 3.6s loop. The asset is
	// two-tone already: primary takes the dark body, secondary the brighter
	// accent, which keeps the swirling depth. Second copy half a cycle behind
	// fills the dip where the loop restarts
	MIASMA("Miasma", BeamRecipe.builder()
		.modelId(29475)
		.animId(7115)
		.primaryFinds(new short[]{-5354, -5224, -6623, -6739})
		.secondaryFinds(new short[]{15254, 15134, 15143, 19404})
		.ambientAdd(30)
		.contrastAdd(30)
		.halfCycleOverlay(true)
		.startPercent(10)
		.build()),

	// Doom of Mokhaiotl's burrow hole glow (loc 50940) — the plume you see when
	// a unique drops. The dirt/hole geometry is hidden so only the glow column
	// is left; primary is the body, secondary the bright tip. The loc model sits
	// off its origin, so the glow gets translated back over the tile centre
	MOKHAIOTL("Mokhaiotl Glow", BeamRecipe.builder()
		.modelId(57324)
		.animId(2755)
		.primaryFinds(new short[]{8134, 8123, 8101})
		.secondaryFinds(new short[]{8152, 8148})
		.hideFinds(new short[]{9098, 8846, 8722, 898, 7700, 7952, 7289, 22470, 16960})
		.translateY(18)
		.translateZ(-70)
		.ambientAdd(64)
		.startPercent(30)
		.build()),
	;

	private final String displayName;
	private final BeamRecipe recipe;

	@Override
	public String toString()
	{
		return displayName;
	}
}
