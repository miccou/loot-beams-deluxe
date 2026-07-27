package com.lootbeamsdeluxe;

import java.awt.Color;
import java.util.List;
import net.runelite.api.TileItem;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TierResolverTest
{
	private static final Color P = Color.RED;
	private static final Color S = Color.BLUE;

	private static final int MAIN = 0;
	private static final int IRONMAN = 1;

	private static TierResolver resolver(ValueMode mode, boolean showHighlightBeam, TierResolver.TierDef... tiers)
	{
		return resolver(mode, OwnershipFilter.ALL, showHighlightBeam, tiers);
	}

	private static TierResolver resolver(ValueMode mode, OwnershipFilter filter, boolean showHighlightBeam, TierResolver.TierDef... tiers)
	{
		return new TierResolver(List.of(tiers), mode, filter, showHighlightBeam, P, S, BeamStyle.MODERN);
	}

	private static TierResolver.TierDef tier(int index, int threshold)
	{
		return new TierResolver.TierDef(index, threshold, P, S, BeamStyle.MODERN);
	}

	private static TrackedItem item(String name, int quantity, int gePrice, int haPrice)
	{
		return item(name, quantity, gePrice, haPrice, TileItem.OWNERSHIP_SELF);
	}

	private static TrackedItem item(String name, int quantity, int gePrice, int haPrice, int ownership)
	{
		return TrackedItem.builder()
			.itemId(1)
			.name(name)
			.quantity(quantity)
			.gePrice(gePrice)
			.haPrice(haPrice)
			.worldViewId(-1)
			.ownership(ownership)
			.build();
	}

	private static ItemListSync lists(String highlighted, String hidden)
	{
		ItemListSync sync = new ItemListSync();
		sync.rebuild(highlighted, hidden, "", true);
		return sync;
	}

	@Test
	public void highestQualifyingTierWins()
	{
		TierResolver resolver = resolver(ValueMode.HIGHEST, true,
			tier(1, 100), tier(2, 1_000), tier(3, 10_000));

		Resolution resolution = resolver.resolve(List.of(item("Shark", 1, 2_500, 0)), lists("", ""), MAIN);
		assertEquals(Resolution.Kind.TIER, resolution.getKind());
		assertEquals(2, resolution.getTierIndex());
	}

	@Test
	public void thresholdIsInclusive()
	{
		TierResolver resolver = resolver(ValueMode.HIGHEST, true, tier(1, 100));

		assertEquals(Resolution.Kind.TIER, resolver.resolve(List.of(item("Shark", 1, 100, 0)), lists("", ""), MAIN).getKind());
		assertEquals(Resolution.Kind.NONE, resolver.resolve(List.of(item("Shark", 1, 99, 0)), lists("", ""), MAIN).getKind());
	}

	@Test
	public void disabledTiersAreSkipped()
	{
		TierResolver resolver = resolver(ValueMode.HIGHEST, true, tier(1, 0), tier(2, 1_000));

		assertEquals(Resolution.Kind.NONE, resolver.resolve(List.of(item("Shark", 1, 500, 0)), lists("", ""), MAIN).getKind());
	}

	@Test
	public void highlightedItemBeatsValueTiers()
	{
		TierResolver resolver = resolver(ValueMode.HIGHEST, true, tier(1, 100));

		Resolution resolution = resolver.resolve(
			List.of(item("Vial", 1, 1, 1), item("Big loot", 1, 1_000_000, 0)),
			lists("Vial", ""), MAIN);
		assertEquals(Resolution.Kind.HIGHLIGHTED, resolution.getKind());
	}

	@Test
	public void highlightBeamDisabledFallsBackToValue()
	{
		TierResolver resolver = resolver(ValueMode.HIGHEST, false, tier(1, 100));

		Resolution resolution = resolver.resolve(List.of(item("Vial", 1, 500, 0)), lists("Vial", ""), MAIN);
		assertEquals(Resolution.Kind.TIER, resolution.getKind());
	}

	@Test
	public void hiddenItemsDoNotCountTowardsValue()
	{
		TierResolver resolver = resolver(ValueMode.HIGHEST, true, tier(1, 100));

		Resolution resolution = resolver.resolve(List.of(item("Bones", 1, 5_000, 0)), lists("", "Bones"), MAIN);
		assertEquals(Resolution.Kind.NONE, resolution.getKind());
	}

	@Test
	public void stackValueMultipliesQuantity()
	{
		TierResolver resolver = resolver(ValueMode.HIGHEST, true, tier(1, 1_000));

		assertEquals(Resolution.Kind.NONE, resolver.resolve(List.of(item("Feather", 1, 2, 0)), lists("", ""), MAIN).getKind());
		assertEquals(Resolution.Kind.TIER, resolver.resolve(List.of(item("Feather", 500, 2, 0)), lists("", ""), MAIN).getKind());
	}

	@Test
	public void valueModesSelectTheRightPrice()
	{
		TrackedItem geHeavy = item("Ge item", 1, 1_000, 10);

		assertEquals(Resolution.Kind.TIER,
			resolver(ValueMode.GRAND_EXCHANGE, true, tier(1, 500)).resolve(List.of(geHeavy), lists("", ""), MAIN).getKind());
		assertEquals(Resolution.Kind.NONE,
			resolver(ValueMode.HIGH_ALCHEMY, true, tier(1, 500)).resolve(List.of(geHeavy), lists("", ""), MAIN).getKind());
		assertEquals(Resolution.Kind.TIER,
			resolver(ValueMode.HIGHEST, true, tier(1, 500)).resolve(List.of(geHeavy), lists("", ""), MAIN).getKind());
	}

	@Test
	public void bestItemOnTileDecides()
	{
		TierResolver resolver = resolver(ValueMode.HIGHEST, true, tier(1, 100), tier(2, 10_000));

		Resolution resolution = resolver.resolve(
			List.of(item("Cheap", 1, 150, 0), item("Pricey", 1, 50_000, 0)),
			lists("", ""), MAIN);
		assertEquals(2, resolution.getTierIndex());
	}

	@Test
	public void dropsFilterIgnoresOtherPlayersLoot()
	{
		TierResolver resolver = resolver(ValueMode.HIGHEST, OwnershipFilter.DROPS, true, tier(1, 100));

		assertEquals(Resolution.Kind.NONE, resolver.resolve(
			List.of(item("Pricey", 1, 50_000, 0, TileItem.OWNERSHIP_OTHER)), lists("", ""), MAIN).getKind());
		assertEquals(Resolution.Kind.TIER, resolver.resolve(
			List.of(item("Pricey", 1, 50_000, 0, TileItem.OWNERSHIP_GROUP)), lists("", ""), MAIN).getKind());
	}

	@Test
	public void takeableFilterDependsOnAccountType()
	{
		TierResolver resolver = resolver(ValueMode.HIGHEST, OwnershipFilter.TAKEABLE, true, tier(1, 100));
		List<TrackedItem> otherPlayersLoot = List.of(item("Pricey", 1, 50_000, 0, TileItem.OWNERSHIP_OTHER));

		assertEquals(Resolution.Kind.TIER, resolver.resolve(otherPlayersLoot, lists("", ""), MAIN).getKind());
		assertEquals(Resolution.Kind.NONE, resolver.resolve(otherPlayersLoot, lists("", ""), IRONMAN).getKind());
	}

	@Test
	public void filteredItemsCannotTriggerTheHighlightBeam()
	{
		TierResolver resolver = resolver(ValueMode.HIGHEST, OwnershipFilter.DROPS, true, tier(1, 100));

		assertEquals(Resolution.Kind.NONE, resolver.resolve(
			List.of(item("Vial", 1, 1, 1, TileItem.OWNERSHIP_OTHER)), lists("Vial", ""), MAIN).getKind());
	}

	@Test
	public void allFilterCountsEverything()
	{
		TierResolver resolver = resolver(ValueMode.HIGHEST, OwnershipFilter.ALL, true, tier(1, 100));

		assertEquals(Resolution.Kind.TIER, resolver.resolve(
			List.of(item("Pricey", 1, 50_000, 0, TileItem.OWNERSHIP_OTHER)), lists("", ""), IRONMAN).getKind());
	}

	@Test
	public void emptyTileHasNoBeam()
	{
		TierResolver resolver = resolver(ValueMode.HIGHEST, true, tier(1, 100));

		assertEquals(Resolution.Kind.NONE, resolver.resolve(List.of(), lists("", ""), MAIN).getKind());
	}
}
