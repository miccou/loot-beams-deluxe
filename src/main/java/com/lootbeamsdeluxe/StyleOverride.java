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
import javax.annotation.Nullable;

@Getter
@RequiredArgsConstructor
public enum StyleOverride
{
	DEFAULT("Same as default", null),
	LIGHT("Light", BeamStyle.LIGHT),
	MODERN("Modern", BeamStyle.MODERN),
	FIRE("Fire", BeamStyle.FIRE),
	ELECTRIC("Electric", BeamStyle.ELECTRIC),
	CLOUD("Cloud", BeamStyle.CLOUD),
	SMOKE("Smoke", BeamStyle.SMOKE),
	MIASMA("Miasma", BeamStyle.MIASMA),
	MOKHAIOTL("Mokhaiotl glow", BeamStyle.MOKHAIOTL),
	LEVEL_UP_FIREWORKS("Level up fireworks", BeamStyle.LEVEL_UP_FIREWORKS),
	SHADES("Dancing shade", BeamStyle.SHADES),
	QUEST_CAPE_UFO("Quest cape", BeamStyle.QUEST_CAPE_UFO),
	KREE_TORNADO("Kree'arra tornado", BeamStyle.KREE_TORNADO),
	SUN_KERIS("Sun keris spec", BeamStyle.SUN_KERIS),
	YAMA_SHADOW("Yama shadow", BeamStyle.YAMA_SHADOW),
	;

	private final String displayName;

	@Nullable
	private final BeamStyle style;

	BeamStyle resolve(BeamStyle defaultStyle)
	{
		return style == null ? defaultStyle : style;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
