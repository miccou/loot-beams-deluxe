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

import lombok.Builder;
import lombok.Value;

/**
 * Everything needed to build one effect: which cache model and anim, which
 * face-colour groups the two colour pickers replace, and any fixed tweaks
 * (baked recolours, transparency, scale, lighting) on top of the raw asset.
 */
@Value
@Builder
class BeamRecipe
{
	int modelId;
	int animId;

	// merged onto the main model, for effects made of several body parts
	@Builder.Default
	int[] extraModelIds = {};

	// face colours (packed HSL) the primary colour picker replaces
	@Builder.Default
	short[] primaryFinds = {};

	// added to the picked colour's saturation (0-7 scale) after conversion
	@Builder.Default
	int primarySaturationAdd = 0;

	// same pair again for the secondary picker
	@Builder.Default
	short[] secondaryFinds = {};

	@Builder.Default
	int secondarySaturationAdd = 0;

	// recolours applied no matter what the pickers say (e.g. a spotanim's own recolour table)
	@Builder.Default
	short[] bakedFinds = {};

	@Builder.Default
	short[] bakedReplaces = {};

	// face colours to hide entirely (e.g. the log pile under the fire)
	@Builder.Default
	short[] hideFinds = {};

	// 0 (opaque) to 255, written into every face; -1 keeps the cache values
	@Builder.Default
	int transparency = -1;

	// added to each face's own transparency, so a model that already fades
	// out in places keeps its fade
	@Builder.Default
	int transparencyAdd = 0;

	// added on top of the client's default ambient/contrast when lighting
	@Builder.Default
	int ambientAdd = 0;

	@Builder.Default
	int contrastAdd = 0;

	// vertex scale in 1/128ths, so 128 = unchanged
	@Builder.Default
	int scaleX = 128;

	@Builder.Default
	int scaleY = 128;

	@Builder.Default
	int scaleZ = 128;

	// vertex translation in model units, applied after scale. Loc models sit
	// off their origin, so beams built from one need recentring over the tile.
	// X/Z are horizontal, Y is vertical (negative = up)
	@Builder.Default
	int translateX = 0;

	@Builder.Default
	int translateY = 0;

	@Builder.Default
	int translateZ = 0;

	@Builder.Default
	boolean loop = true;

	// animation seeking on start
	@Builder.Default
	int startPercent = 0;

	// where a loop restarts, for anims that open with blank frames
	@Builder.Default
	int loopPercent = 0;

	// start the next pass on a second copy this long before the current one
	// ends, so the passes overlap instead of cutting from one to the next
	@Builder.Default
	int loopOverlapMs = 0;

	// one-shot anims finish here, for ones with a messy last few frames
	@Builder.Default
	int endPercent = 100;

	// jump from skipFromPercent straight to skipToPercent, cutting out a dead
	// stretch in the middle of an anim. Off while skipToPercent is 0
	@Builder.Default
	int skipFromPercent = 0;

	@Builder.Default
	int skipToPercent = 0;

	// playback speed, 150 = 1.5x
	@Builder.Default
	int speedPercent = 100;

	// render a second copy half an anim cycle behind, covering the dip when the loop restarts
	@Builder.Default
	boolean halfCycleOverlay = false;

	// scales every animated frame in 1/128ths, the way a spotanim's own resize
	// works. Unlike scaleX/Y/Z it shrinks movement baked into the anim too
	@Builder.Default
	int resize = 128;

	boolean hasScale()
	{
		return scaleX != 128 || scaleY != 128 || scaleZ != 128;
	}

	boolean hasTranslate()
	{
		return translateX != 0 || translateY != 0 || translateZ != 0;
	}
}
