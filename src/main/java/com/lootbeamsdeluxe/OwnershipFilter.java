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
import net.runelite.api.TileItem;

/**
 * Which items on a tile count towards its beam. 
 * Names match Ground Items OwnershipFilterMode so SYNC can resolve its setting by name.
 */
@Getter
@RequiredArgsConstructor
public enum OwnershipFilter
{
	SYNC("Ground Items"),
	ALL("All"),
	TAKEABLE("Takeable"),
	DROPS("Drops"),
	;

	private final String displayName;

	// unset/unknown falls back to ALL
	static OwnershipFilter fromGroundItems(@Nullable String configValue)
	{
		if (configValue != null)
		{
			String trimmed = configValue.trim();
			for (OwnershipFilter filter : values())
			{
				if (filter != SYNC && filter.name().equalsIgnoreCase(trimmed))
				{
					return filter;
				}
			}
		}

		return ALL;
	}

	// All      -> none | self | other | group
	// Drops    -> self | group
	// Takeable -> none | self | group | (if a main then other)
	boolean shouldCount(int ownership, int accountType)
	{
		switch (this)
		{
			case DROPS:
				return ownership == TileItem.OWNERSHIP_SELF || ownership == TileItem.OWNERSHIP_GROUP;
			case TAKEABLE:
				return ownership != TileItem.OWNERSHIP_OTHER || accountType == 0; // mains can always take items
			default:
				return true;
		}
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
