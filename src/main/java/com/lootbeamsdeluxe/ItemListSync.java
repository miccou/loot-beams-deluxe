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

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;

/**
 * The highlight/hide lists. With sync on, these mirror Ground Items' lists
 * (read live from its config group) merged with our own extra highlight list.
 * Matching works exactly like Ground Items': exact highlight beats exact hide
 * beats wildcard highlight beats wildcard hide.
 */
class ItemListSync
{
	static final int NONE = 0;
	static final int HIGHLIGHTED = 1;
	static final int HIDDEN = 2;

	private static final int MATCH_NONE = 0;
	private static final int MATCH_WILDCARD = 1;
	private static final int MATCH_EXACT = 2;

	private List<ItemThreshold> highlights = List.of();
	private List<ItemThreshold> hidden = List.of();

	void rebuild(String groundItemsHighlighted, String groundItemsHidden, String additionalHighlights, boolean syncGroundItems)
	{
		List<String> highlightEntries = new ArrayList<>();
		List<String> hiddenEntries = new ArrayList<>();

		if (syncGroundItems)
		{
			highlightEntries.addAll(Text.fromCSV(Strings.nullToEmpty(groundItemsHighlighted)));
			hiddenEntries.addAll(Text.fromCSV(Strings.nullToEmpty(groundItemsHidden)));
		}
		highlightEntries.addAll(Text.fromCSV(Strings.nullToEmpty(additionalHighlights)));

		highlights = parse(highlightEntries);
		hidden = parse(hiddenEntries);
	}

	private static List<ItemThreshold> parse(List<String> entries)
	{
		return entries.stream()
			.map(ItemThreshold::fromName)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	int hiddenOrHighlighted(String name, int quantity)
	{
		int hl = matches(highlights, name, quantity);
		if (hl == MATCH_EXACT)
		{
			return HIGHLIGHTED;
		}

		int hi = matches(hidden, name, quantity);
		if (hi == MATCH_EXACT)
		{
			return HIDDEN;
		}

		if (hl == MATCH_WILDCARD)
		{
			return HIGHLIGHTED;
		}
		if (hi == MATCH_WILDCARD)
		{
			return HIDDEN;
		}

		return NONE;
	}

	private static int matches(List<ItemThreshold> items, String name, int quantity)
	{
		for (ItemThreshold it : items)
		{
			if (!it.isWildcard()
				&& it.getName().equalsIgnoreCase(name)
				&& it.quantityHolds(quantity))
			{
				return MATCH_EXACT;
			}
		}

		for (ItemThreshold it : items)
		{
			if (it.isWildcard()
				&& WildcardMatcher.matches(it.getName(), name)
				&& it.quantityHolds(quantity))
			{
				return MATCH_WILDCARD;
			}
		}

		return MATCH_NONE;
	}
}
