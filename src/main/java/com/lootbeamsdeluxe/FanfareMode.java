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

/**
 * When to play the drop fanfare. Tier modes mean "that tier and above",
 * and all of them include highlighted items.
 */
@Getter
@RequiredArgsConstructor
public enum FanfareMode
{
	OFF("Off", Integer.MAX_VALUE, false),
	HIGHLIGHTED_ONLY("Highlighted items only", Integer.MAX_VALUE, true),
	TIER_8("Tier 8 and highlighted", 8, true),
	TIER_7("Tier 7 and above", 7, true),
	TIER_6("Tier 6 and above", 6, true),
	TIER_5("Tier 5 and above", 5, true),
	TIER_4("Tier 4 and above", 4, true),
	TIER_3("Tier 3 and above", 3, true),
	TIER_2("Tier 2 and above", 2, true),
	TIER_1("Tier 1 and above", 1, true),
	;

	private final String displayName;
	private final int minTier;
	private final boolean includesHighlighted;

	boolean qualifies(Resolution resolution)
	{
		switch (resolution.getKind())
		{
			case HIGHLIGHTED:
				return includesHighlighted;
			case TIER:
				return resolution.getTierIndex() >= minTier;
			default:
				return false;
		}
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
