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
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(LootBeamsDeluxeConfig.GROUP)
public interface LootBeamsDeluxeConfig extends Config
{
	String GROUP = "lootbeamsdeluxe";

	@ConfigItem(
		keyName = "priceMode",
		name = "Price mode",
		description = "Which value decides an item's tier: Grand Exchange price, High Alchemy value, or whichever is higher.",
		position = 0
	)
	default ValueMode priceMode()
	{
		return ValueMode.HIGHEST;
	}

	@ConfigItem(
		keyName = "defaultStyle",
		name = "Default style",
		description = "Beam style used by tiers set to 'Same as default'.",
		position = 1
	)
	default BeamStyle defaultStyle()
	{
		return BeamStyle.MODERN;
	}

	@ConfigItem(
		keyName = "fanfareStyle",
		name = "Default fanfare",
		description = "Entrance effect used by tiers set to 'Same as default'.",
		position = 2
	)
	default FanfareStyle fanfareStyle()
	{
		return FanfareStyle.LIGHTNING;
	}

	@ConfigItem(
		keyName = "syncGroundItems",
		name = "Use Ground Items lists",
		description = "Reuse the highlighted and hidden item lists from the Ground Items plugin, so you only manage one set of lists.",
		position = 3
	)
	default boolean syncGroundItems()
	{
		return true;
	}

	@ConfigItem(
		keyName = "ownershipFilter",
		name = "Ownership filter",
		description = "Which items on a tile count towards its beam. 'Drops' is yours and your group's only, "
			+ "'Takeable' also hides what an ironman can't pick up. Follows Ground Items' own setting by default.",
		position = 4
	)
	default OwnershipFilter ownershipFilter()
	{
		return OwnershipFilter.SYNC;
	}

	@ConfigItem(
		keyName = "additionalHighlights",
		name = "Also highlight",
		description = "Extra items that always get the highlight beam (comma separated, supports * wildcards and name>quantity).",
		position = 5
	)
	default String additionalHighlights()
	{
		return "";
	}

	// -- Highlighted items ---------------------------------------------------

	@ConfigSection(
		name = "Highlighted items",
		description = "Beam and fanfare settings for explicitly highlighted items.",
		position = 10,
		closedByDefault = true
	)
	String highlightedSection = "highlightedSection";

	@ConfigItem(
		keyName = "showHighlightBeam",
		name = "Show beam",
		description = "Show a beam for items on your highlight lists, regardless of value.",
		position = 0,
		section = highlightedSection
	)
	default boolean showHighlightBeam()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "highlightBeamColor",
		name = "Beam color",
		description = "Main beam color for highlighted items.",
		position = 1,
		section = highlightedSection
	)
	default Color highlightBeamColor()
	{
		return new Color(0xAA00FF);
	}

	@Alpha
	@ConfigItem(
		keyName = "highlightSecondaryColor",
		name = "Secondary color",
		description = "Second color of the effect: the lattice (Modern), bands (Fire), bolts (Electric), saucer (Quest cape), streaks (Kree'arra tornado), claws (Yama shadow), half the bursts (Level up fireworks), or bright accents (Smoke, Miasma, Mokhaiotl glow). Light, Cloud, Dancing shade and Sun keris spec are single-color.",
		position = 2,
		section = highlightedSection
	)
	default Color highlightSecondaryColor()
	{
		return new Color(0xCC66FF);
	}

	@ConfigItem(
		keyName = "highlightStyle",
		name = "Style",
		description = "Beam style for highlighted items.",
		position = 3,
		section = highlightedSection
	)
	default StyleOverride highlightStyle()
	{
		return StyleOverride.DEFAULT;
	}

	@ConfigItem(
		keyName = "highlightFanfare",
		name = "Fanfare",
		description = "Entrance effect played on the tile when a highlighted item drops, before the beam starts.",
		position = 4,
		section = highlightedSection
	)
	default FanfareOverride highlightFanfare()
	{
		return FanfareOverride.OFF;
	}

	// -- Tier 1 ---------------------------------------------------------------

	@ConfigSection(
		name = "Tier 1 (lowest)",
		description = "Threshold, colors, style, and fanfare for this tier.",
		position = 11,
		closedByDefault = true
	)
	String tier1Section = "tier1Section";

	@ConfigItem(
		keyName = "tier1Value",
		name = "Value",
		description = "Minimum total stack value for this tier. 0 disables the tier.",
		position = 0,
		section = tier1Section
	)
	@Units(" gp")
	default int tier1Value()
	{
		return 20_000;
	}

	@Alpha
	@ConfigItem(
		keyName = "tier1BeamColor",
		name = "Beam color",
		description = "Main beam color for this tier.",
		position = 1,
		section = tier1Section
	)
	default Color tier1BeamColor()
	{
		return new Color(0x66B2FF);
	}

	@Alpha
	@ConfigItem(
		keyName = "tier1SecondaryColor",
		name = "Secondary color",
		description = "Second color of the effect: the lattice (Modern), bands (Fire), bolts (Electric), saucer (Quest cape), streaks (Kree'arra tornado), claws (Yama shadow), half the bursts (Level up fireworks), or bright accents (Smoke, Miasma, Mokhaiotl glow). Light, Cloud, Dancing shade and Sun keris spec are single-color.",
		position = 2,
		section = tier1Section
	)
	default Color tier1SecondaryColor()
	{
		return new Color(0x99CCFF);
	}

	@ConfigItem(
		keyName = "tier1Style",
		name = "Style",
		description = "Beam style for this tier.",
		position = 3,
		section = tier1Section
	)
	default StyleOverride tier1Style()
	{
		return StyleOverride.DEFAULT;
	}

	@ConfigItem(
		keyName = "tier1Fanfare",
		name = "Fanfare",
		description = "Entrance effect played on the tile when loot of this tier drops, before the beam starts.",
		position = 4,
		section = tier1Section
	)
	default FanfareOverride tier1Fanfare()
	{
		return FanfareOverride.OFF;
	}

	// -- Tier 2 ---------------------------------------------------------------

	@ConfigSection(
		name = "Tier 2",
		description = "Threshold, colors, style, and fanfare for this tier.",
		position = 12,
		closedByDefault = true
	)
	String tier2Section = "tier2Section";

	@ConfigItem(
		keyName = "tier2Value",
		name = "Value",
		description = "Minimum total stack value for this tier. 0 disables the tier.",
		position = 0,
		section = tier2Section
	)
	@Units(" gp")
	default int tier2Value()
	{
		return 100_000;
	}

	@Alpha
	@ConfigItem(
		keyName = "tier2BeamColor",
		name = "Beam color",
		description = "Main beam color for this tier.",
		position = 1,
		section = tier2Section
	)
	default Color tier2BeamColor()
	{
		return new Color(0x99FF99);
	}

	@Alpha
	@ConfigItem(
		keyName = "tier2SecondaryColor",
		name = "Secondary color",
		description = "Second color of the effect: the lattice (Modern), bands (Fire), bolts (Electric), saucer (Quest cape), streaks (Kree'arra tornado), claws (Yama shadow), half the bursts (Level up fireworks), or bright accents (Smoke, Miasma, Mokhaiotl glow). Light, Cloud, Dancing shade and Sun keris spec are single-color.",
		position = 2,
		section = tier2Section
	)
	default Color tier2SecondaryColor()
	{
		return new Color(0xCCFFCC);
	}

	@ConfigItem(
		keyName = "tier2Style",
		name = "Style",
		description = "Beam style for this tier.",
		position = 3,
		section = tier2Section
	)
	default StyleOverride tier2Style()
	{
		return StyleOverride.DEFAULT;
	}

	@ConfigItem(
		keyName = "tier2Fanfare",
		name = "Fanfare",
		description = "Entrance effect played on the tile when loot of this tier drops, before the beam starts.",
		position = 4,
		section = tier2Section
	)
	default FanfareOverride tier2Fanfare()
	{
		return FanfareOverride.OFF;
	}

	// -- Tier 3 ---------------------------------------------------------------

	@ConfigSection(
		name = "Tier 3",
		description = "Threshold, colors, style, and fanfare for this tier.",
		position = 13,
		closedByDefault = true
	)
	String tier3Section = "tier3Section";

	@ConfigItem(
		keyName = "tier3Value",
		name = "Value",
		description = "Minimum total stack value for this tier. 0 disables the tier.",
		position = 0,
		section = tier3Section
	)
	@Units(" gp")
	default int tier3Value()
	{
		return 500_000;
	}

	@Alpha
	@ConfigItem(
		keyName = "tier3BeamColor",
		name = "Beam color",
		description = "Main beam color for this tier.",
		position = 1,
		section = tier3Section
	)
	default Color tier3BeamColor()
	{
		return new Color(0x00E5D0);
	}

	@Alpha
	@ConfigItem(
		keyName = "tier3SecondaryColor",
		name = "Secondary color",
		description = "Second color of the effect: the lattice (Modern), bands (Fire), bolts (Electric), saucer (Quest cape), streaks (Kree'arra tornado), claws (Yama shadow), half the bursts (Level up fireworks), or bright accents (Smoke, Miasma, Mokhaiotl glow). Light, Cloud, Dancing shade and Sun keris spec are single-color.",
		position = 2,
		section = tier3Section
	)
	default Color tier3SecondaryColor()
	{
		return new Color(0x66FFF0);
	}

	@ConfigItem(
		keyName = "tier3Style",
		name = "Style",
		description = "Beam style for this tier.",
		position = 3,
		section = tier3Section
	)
	default StyleOverride tier3Style()
	{
		return StyleOverride.DEFAULT;
	}

	@ConfigItem(
		keyName = "tier3Fanfare",
		name = "Fanfare",
		description = "Entrance effect played on the tile when loot of this tier drops, before the beam starts.",
		position = 4,
		section = tier3Section
	)
	default FanfareOverride tier3Fanfare()
	{
		return FanfareOverride.OFF;
	}

	// -- Tier 4 ---------------------------------------------------------------

	@ConfigSection(
		name = "Tier 4",
		description = "Threshold, colors, style, and fanfare for this tier.",
		position = 14,
		closedByDefault = true
	)
	String tier4Section = "tier4Section";

	@ConfigItem(
		keyName = "tier4Value",
		name = "Value",
		description = "Minimum total stack value for this tier. 0 disables the tier.",
		position = 0,
		section = tier4Section
	)
	@Units(" gp")
	default int tier4Value()
	{
		return 1_000_000;
	}

	@Alpha
	@ConfigItem(
		keyName = "tier4BeamColor",
		name = "Beam color",
		description = "Main beam color for this tier.",
		position = 1,
		section = tier4Section
	)
	default Color tier4BeamColor()
	{
		return new Color(0xFFB000);
	}

	@Alpha
	@ConfigItem(
		keyName = "tier4SecondaryColor",
		name = "Secondary color",
		description = "Second color of the effect: the lattice (Modern), bands (Fire), bolts (Electric), saucer (Quest cape), streaks (Kree'arra tornado), claws (Yama shadow), half the bursts (Level up fireworks), or bright accents (Smoke, Miasma, Mokhaiotl glow). Light, Cloud, Dancing shade and Sun keris spec are single-color.",
		position = 2,
		section = tier4Section
	)
	default Color tier4SecondaryColor()
	{
		return new Color(0xFFD066);
	}

	@ConfigItem(
		keyName = "tier4Style",
		name = "Style",
		description = "Beam style for this tier.",
		position = 3,
		section = tier4Section
	)
	default StyleOverride tier4Style()
	{
		return StyleOverride.DEFAULT;
	}

	@ConfigItem(
		keyName = "tier4Fanfare",
		name = "Fanfare",
		description = "Entrance effect played on the tile when loot of this tier drops, before the beam starts.",
		position = 4,
		section = tier4Section
	)
	default FanfareOverride tier4Fanfare()
	{
		return FanfareOverride.OFF;
	}

	// -- Tier 5 ---------------------------------------------------------------

	@ConfigSection(
		name = "Tier 5",
		description = "Threshold, colors, style, and fanfare for this tier.",
		position = 15,
		closedByDefault = true
	)
	String tier5Section = "tier5Section";

	@ConfigItem(
		keyName = "tier5Value",
		name = "Value",
		description = "Minimum total stack value for this tier. 0 disables the tier.",
		position = 0,
		section = tier5Section
	)
	@Units(" gp")
	default int tier5Value()
	{
		return 5_000_000;
	}

	@Alpha
	@ConfigItem(
		keyName = "tier5BeamColor",
		name = "Beam color",
		description = "Main beam color for this tier.",
		position = 1,
		section = tier5Section
	)
	default Color tier5BeamColor()
	{
		return new Color(0xFF6040);
	}

	@Alpha
	@ConfigItem(
		keyName = "tier5SecondaryColor",
		name = "Secondary color",
		description = "Second color of the effect: the lattice (Modern), bands (Fire), bolts (Electric), saucer (Quest cape), streaks (Kree'arra tornado), claws (Yama shadow), half the bursts (Level up fireworks), or bright accents (Smoke, Miasma, Mokhaiotl glow). Light, Cloud, Dancing shade and Sun keris spec are single-color.",
		position = 2,
		section = tier5Section
	)
	default Color tier5SecondaryColor()
	{
		return new Color(0xFF9980);
	}

	@ConfigItem(
		keyName = "tier5Style",
		name = "Style",
		description = "Beam style for this tier.",
		position = 3,
		section = tier5Section
	)
	default StyleOverride tier5Style()
	{
		return StyleOverride.DEFAULT;
	}

	@ConfigItem(
		keyName = "tier5Fanfare",
		name = "Fanfare",
		description = "Entrance effect played on the tile when loot of this tier drops, before the beam starts.",
		position = 4,
		section = tier5Section
	)
	default FanfareOverride tier5Fanfare()
	{
		return FanfareOverride.OFF;
	}

	// -- Tier 6 ---------------------------------------------------------------

	@ConfigSection(
		name = "Tier 6",
		description = "Threshold, colors, style, and fanfare for this tier.",
		position = 16,
		closedByDefault = true
	)
	String tier6Section = "tier6Section";

	@ConfigItem(
		keyName = "tier6Value",
		name = "Value",
		description = "Minimum total stack value for this tier. 0 disables the tier.",
		position = 0,
		section = tier6Section
	)
	@Units(" gp")
	default int tier6Value()
	{
		return 10_000_000;
	}

	@Alpha
	@ConfigItem(
		keyName = "tier6BeamColor",
		name = "Beam color",
		description = "Main beam color for this tier.",
		position = 1,
		section = tier6Section
	)
	default Color tier6BeamColor()
	{
		return new Color(0xFF66E0);
	}

	@Alpha
	@ConfigItem(
		keyName = "tier6SecondaryColor",
		name = "Secondary color",
		description = "Second color of the effect: the lattice (Modern), bands (Fire), bolts (Electric), saucer (Quest cape), streaks (Kree'arra tornado), claws (Yama shadow), half the bursts (Level up fireworks), or bright accents (Smoke, Miasma, Mokhaiotl glow). Light, Cloud, Dancing shade and Sun keris spec are single-color.",
		position = 2,
		section = tier6Section
	)
	default Color tier6SecondaryColor()
	{
		return new Color(0xFF99EC);
	}

	@ConfigItem(
		keyName = "tier6Style",
		name = "Style",
		description = "Beam style for this tier.",
		position = 3,
		section = tier6Section
	)
	default StyleOverride tier6Style()
	{
		return StyleOverride.DEFAULT;
	}

	@ConfigItem(
		keyName = "tier6Fanfare",
		name = "Fanfare",
		description = "Entrance effect played on the tile when loot of this tier drops, before the beam starts.",
		position = 4,
		section = tier6Section
	)
	default FanfareOverride tier6Fanfare()
	{
		return FanfareOverride.OFF;
	}

	// -- Tier 7 ---------------------------------------------------------------

	@ConfigSection(
		name = "Tier 7",
		description = "Threshold, colors, style, and fanfare for this tier.",
		position = 17,
		closedByDefault = true
	)
	String tier7Section = "tier7Section";

	@ConfigItem(
		keyName = "tier7Value",
		name = "Value",
		description = "Minimum total stack value for this tier. 0 disables the tier.",
		position = 0,
		section = tier7Section
	)
	@Units(" gp")
	default int tier7Value()
	{
		return 50_000_000;
	}

	@Alpha
	@ConfigItem(
		keyName = "tier7BeamColor",
		name = "Beam color",
		description = "Main beam color for this tier.",
		position = 1,
		section = tier7Section
	)
	default Color tier7BeamColor()
	{
		return new Color(0xB266FF);
	}

	@Alpha
	@ConfigItem(
		keyName = "tier7SecondaryColor",
		name = "Secondary color",
		description = "Second color of the effect: the lattice (Modern), bands (Fire), bolts (Electric), saucer (Quest cape), streaks (Kree'arra tornado), claws (Yama shadow), half the bursts (Level up fireworks), or bright accents (Smoke, Miasma, Mokhaiotl glow). Light, Cloud, Dancing shade and Sun keris spec are single-color.",
		position = 2,
		section = tier7Section
	)
	default Color tier7SecondaryColor()
	{
		return new Color(0xD1A6FF);
	}

	@ConfigItem(
		keyName = "tier7Style",
		name = "Style",
		description = "Beam style for this tier.",
		position = 3,
		section = tier7Section
	)
	default StyleOverride tier7Style()
	{
		return StyleOverride.DEFAULT;
	}

	@ConfigItem(
		keyName = "tier7Fanfare",
		name = "Fanfare",
		description = "Entrance effect played on the tile when loot of this tier drops, before the beam starts.",
		position = 4,
		section = tier7Section
	)
	default FanfareOverride tier7Fanfare()
	{
		return FanfareOverride.OFF;
	}

	// -- Tier 8 ---------------------------------------------------------------

	@ConfigSection(
		name = "Tier 8 (highest)",
		description = "Threshold, colors, style, and fanfare for this tier.",
		position = 18,
		closedByDefault = true
	)
	String tier8Section = "tier8Section";

	@ConfigItem(
		keyName = "tier8Value",
		name = "Value",
		description = "Minimum total stack value for this tier. 0 disables the tier.",
		position = 0,
		section = tier8Section
	)
	@Units(" gp")
	default int tier8Value()
	{
		return 100_000_000;
	}

	@Alpha
	@ConfigItem(
		keyName = "tier8BeamColor",
		name = "Beam color",
		description = "Main beam color for this tier.",
		position = 1,
		section = tier8Section
	)
	default Color tier8BeamColor()
	{
		return new Color(0xF5F5F5);
	}

	@Alpha
	@ConfigItem(
		keyName = "tier8SecondaryColor",
		name = "Secondary color",
		description = "Second color of the effect: the lattice (Modern), bands (Fire), bolts (Electric), saucer (Quest cape), streaks (Kree'arra tornado), claws (Yama shadow), half the bursts (Level up fireworks), or bright accents (Smoke, Miasma, Mokhaiotl glow). Light, Cloud, Dancing shade and Sun keris spec are single-color.",
		position = 2,
		section = tier8Section
	)
	default Color tier8SecondaryColor()
	{
		return new Color(0xFFF0B0);
	}

	@ConfigItem(
		keyName = "tier8Style",
		name = "Style",
		description = "Beam style for this tier.",
		position = 3,
		section = tier8Section
	)
	default StyleOverride tier8Style()
	{
		return StyleOverride.DEFAULT;
	}

	@ConfigItem(
		keyName = "tier8Fanfare",
		name = "Fanfare",
		description = "Entrance effect played on the tile when loot of this tier drops, before the beam starts.",
		position = 4,
		section = tier8Section
	)
	default FanfareOverride tier8Fanfare()
	{
		return FanfareOverride.OFF;
	}

	// -- Preview beams ---------------------------------------------------------

	@ConfigSection(
		name = "Preview beams",
		description = "Put a beam on a tile next to you to preview a tier without waiting for a drop.",
		position = 19,
		closedByDefault = true
	)
	String previewSection = "previewSection";

	@ConfigItem(
		keyName = "previewDirection",
		name = "Direction",
		description = "Which way from you the preview tile sits.",
		position = 0,
		section = previewSection
	)
	default PreviewDirection previewDirection()
	{
		return PreviewDirection.NORTH;
	}

	@ConfigItem(
		keyName = "previewDistance",
		name = "Distance",
		description = "How far from you the preview tile sits.",
		position = 1,
		section = previewSection
	)
	@Units(" tiles")
	@Range(min = 1, max = 25)
	default int previewDistance()
	{
		return 2;
	}

	@ConfigItem(
		keyName = "previewHighlight",
		name = "Highlight",
		description = "Show the highlight beam on the preview tile. Untick to remove it. Wins over any ticked tier.",
		position = 2,
		section = previewSection
	)
	default boolean previewHighlight()
	{
		return false;
	}

	@ConfigItem(
		keyName = "previewTier1",
		name = "Tier 1",
		description = "Show this tier's beam on the preview tile. Untick to remove it.",
		position = 3,
		section = previewSection
	)
	default boolean previewTier1()
	{
		return false;
	}

	@ConfigItem(
		keyName = "previewTier2",
		name = "Tier 2",
		description = "Show this tier's beam on the preview tile. Untick to remove it.",
		position = 4,
		section = previewSection
	)
	default boolean previewTier2()
	{
		return false;
	}

	@ConfigItem(
		keyName = "previewTier3",
		name = "Tier 3",
		description = "Show this tier's beam on the preview tile. Untick to remove it.",
		position = 5,
		section = previewSection
	)
	default boolean previewTier3()
	{
		return false;
	}

	@ConfigItem(
		keyName = "previewTier4",
		name = "Tier 4",
		description = "Show this tier's beam on the preview tile. Untick to remove it.",
		position = 6,
		section = previewSection
	)
	default boolean previewTier4()
	{
		return false;
	}

	@ConfigItem(
		keyName = "previewTier5",
		name = "Tier 5",
		description = "Show this tier's beam on the preview tile. Untick to remove it.",
		position = 7,
		section = previewSection
	)
	default boolean previewTier5()
	{
		return false;
	}

	@ConfigItem(
		keyName = "previewTier6",
		name = "Tier 6",
		description = "Show this tier's beam on the preview tile. Untick to remove it.",
		position = 8,
		section = previewSection
	)
	default boolean previewTier6()
	{
		return false;
	}

	@ConfigItem(
		keyName = "previewTier7",
		name = "Tier 7",
		description = "Show this tier's beam on the preview tile. Untick to remove it.",
		position = 9,
		section = previewSection
	)
	default boolean previewTier7()
	{
		return false;
	}

	@ConfigItem(
		keyName = "previewTier8",
		name = "Tier 8",
		description = "Show this tier's beam on the preview tile. Untick to remove it.",
		position = 10,
		section = previewSection
	)
	default boolean previewTier8()
	{
		return false;
	}
}
