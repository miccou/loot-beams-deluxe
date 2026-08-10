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

/** Per-tier fanfare pick: silent, the default style, or a style of its own. */
@Getter
public enum FanfareOverride
{
	OFF("Off", null),
	DEFAULT("Same as default", null),
	LIGHTNING(FanfareStyle.LIGHTNING),
	RISING_COLUMN(FanfareStyle.RISING_COLUMN),
	TROPHY_TELEPORT(FanfareStyle.TROPHY_TELEPORT),
	DRAKAN_INCINERATE(FanfareStyle.DRAKAN_INCINERATE),
	;

	private final String displayName;

	@Nullable
	private final FanfareStyle style;

	FanfareOverride(String displayName, @Nullable FanfareStyle style)
	{
		this.displayName = displayName;
		this.style = style;
	}

	FanfareOverride(FanfareStyle style)
	{
		this(style.getDisplayName(), style);
	}

	/** Null means this tier plays no fanfare. */
	@Nullable
	FanfareStyle resolve(FanfareStyle defaultStyle)
	{
		if (this == OFF)
		{
			return null;
		}

		return style == null ? defaultStyle : style;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
