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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import lombok.Value;

/** Immutable snapshot of the tier/highlight config — rebuilt whenever config changes. */
class TierResolver
{
	@Value
	static class TierDef
	{
		int index;
		int threshold;
		Color primary;
		Color secondary;
		BeamStyle style;
	}

	private final List<TierDef> tiersDescending;
	private final ValueMode valueMode;
	private final OwnershipFilter ownershipFilter;
	private final boolean showHighlightBeam;
	private final Resolution highlightResolution;

	TierResolver(List<TierDef> tiers, ValueMode valueMode, OwnershipFilter ownershipFilter,
		boolean showHighlightBeam, Color highlightPrimary, Color highlightSecondary, BeamStyle highlightStyle)
	{
		List<TierDef> sorted = new ArrayList<>();
		for (TierDef tier : tiers)
		{
			if (tier.getThreshold() > 0)
			{
				sorted.add(tier);
			}
		}
		sorted.sort(Comparator.comparingInt(TierDef::getThreshold).reversed());
		this.tiersDescending = sorted;
		this.valueMode = valueMode;
		this.ownershipFilter = ownershipFilter;
		this.showHighlightBeam = showHighlightBeam;
		this.highlightResolution = new Resolution(Resolution.Kind.HIGHLIGHTED, 0,
			highlightPrimary, highlightSecondary, highlightStyle);
	}

	static TierResolver fromConfig(LootBeamsDeluxeConfig config, OwnershipFilter ownershipFilter)
	{
		BeamStyle defaultStyle = config.defaultStyle();
		List<TierDef> tiers = List.of(
			new TierDef(1, config.tier1Value(), config.tier1BeamColor(), config.tier1SecondaryColor(), config.tier1Style().resolve(defaultStyle)),
			new TierDef(2, config.tier2Value(), config.tier2BeamColor(), config.tier2SecondaryColor(), config.tier2Style().resolve(defaultStyle)),
			new TierDef(3, config.tier3Value(), config.tier3BeamColor(), config.tier3SecondaryColor(), config.tier3Style().resolve(defaultStyle)),
			new TierDef(4, config.tier4Value(), config.tier4BeamColor(), config.tier4SecondaryColor(), config.tier4Style().resolve(defaultStyle)),
			new TierDef(5, config.tier5Value(), config.tier5BeamColor(), config.tier5SecondaryColor(), config.tier5Style().resolve(defaultStyle)),
			new TierDef(6, config.tier6Value(), config.tier6BeamColor(), config.tier6SecondaryColor(), config.tier6Style().resolve(defaultStyle)),
			new TierDef(7, config.tier7Value(), config.tier7BeamColor(), config.tier7SecondaryColor(), config.tier7Style().resolve(defaultStyle)),
			new TierDef(8, config.tier8Value(), config.tier8BeamColor(), config.tier8SecondaryColor(), config.tier8Style().resolve(defaultStyle)));

		return new TierResolver(tiers, config.priceMode(), ownershipFilter, config.showHighlightBeam(),
			config.highlightBeamColor(), config.highlightSecondaryColor(),
			config.highlightStyle().resolve(defaultStyle));
	}

	// filtered-out items are skipped, highlighted wins outright, hidden is
	// skipped, otherwise the priciest stack on the tile picks the tier.
	// accountType is the IRONMAN varbit
	Resolution resolve(Collection<TrackedItem> items, ItemListSync lists, int accountType)
	{
		long highestValue = -1;

		for (TrackedItem item : items)
		{
			if (!ownershipFilter.shouldCount(item.getOwnership(), accountType))
			{
				continue;
			}

			int hiddenOrHighlighted = lists.hiddenOrHighlighted(item.getName(), item.getQuantity());
			if (hiddenOrHighlighted == ItemListSync.HIGHLIGHTED && showHighlightBeam)
			{
				return highlightResolution;
			}

			if (hiddenOrHighlighted == ItemListSync.HIDDEN)
			{
				continue;
			}

			highestValue = Math.max(highestValue, item.stackValue(valueMode));
		}

		for (TierDef tier : tiersDescending)
		{
			if (highestValue >= tier.getThreshold())
			{
				return new Resolution(Resolution.Kind.TIER, tier.getIndex(),
					tier.getPrimary(), tier.getSecondary(), tier.getStyle());
			}
		}

		return Resolution.NONE;
	}
}
