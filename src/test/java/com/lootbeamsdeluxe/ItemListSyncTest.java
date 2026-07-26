package com.lootbeamsdeluxe;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ItemListSyncTest
{
	@Test
	public void syncMergesGroundItemsAndAdditionalLists()
	{
		ItemListSync sync = new ItemListSync();
		sync.rebuild("Twisted bow", "Vial", "Dragon claws", true);

		assertEquals(ItemListSync.HIGHLIGHTED, sync.hiddenOrHighlighted("Twisted bow", 1));
		assertEquals(ItemListSync.HIGHLIGHTED, sync.hiddenOrHighlighted("Dragon claws", 1));
		assertEquals(ItemListSync.HIDDEN, sync.hiddenOrHighlighted("Vial", 1));
		assertEquals(ItemListSync.NONE, sync.hiddenOrHighlighted("Shark", 1));
	}

	@Test
	public void syncOffIgnoresGroundItemsLists()
	{
		ItemListSync sync = new ItemListSync();
		sync.rebuild("Twisted bow", "Vial", "Dragon claws", false);

		assertEquals(ItemListSync.NONE, sync.hiddenOrHighlighted("Twisted bow", 1));
		assertEquals(ItemListSync.NONE, sync.hiddenOrHighlighted("Vial", 1));
		assertEquals(ItemListSync.HIGHLIGHTED, sync.hiddenOrHighlighted("Dragon claws", 1));
	}

	@Test
	public void nullGroundItemsConfigIsTolerated()
	{
		ItemListSync sync = new ItemListSync();
		sync.rebuild(null, null, "Dragon claws", true);

		assertEquals(ItemListSync.HIGHLIGHTED, sync.hiddenOrHighlighted("Dragon claws", 1));
		assertEquals(ItemListSync.NONE, sync.hiddenOrHighlighted("Shark", 1));
	}

	@Test
	public void exactHighlightBeatsExactHide()
	{
		ItemListSync sync = new ItemListSync();
		sync.rebuild("Shark", "Shark", "", true);

		assertEquals(ItemListSync.HIGHLIGHTED, sync.hiddenOrHighlighted("Shark", 1));
	}

	@Test
	public void exactHideBeatsWildcardHighlight()
	{
		ItemListSync sync = new ItemListSync();
		sync.rebuild("Rune *", "Rune scimitar", "", true);

		assertEquals(ItemListSync.HIDDEN, sync.hiddenOrHighlighted("Rune scimitar", 1));
		assertEquals(ItemListSync.HIGHLIGHTED, sync.hiddenOrHighlighted("Rune platebody", 1));
	}

	@Test
	public void wildcardHighlightBeatsWildcardHide()
	{
		ItemListSync sync = new ItemListSync();
		sync.rebuild("Rune *", "Rune s*", "", true);

		assertEquals(ItemListSync.HIGHLIGHTED, sync.hiddenOrHighlighted("Rune scimitar", 1));
	}

	@Test
	public void quantityThresholdGatesTheMatch()
	{
		ItemListSync sync = new ItemListSync();
		sync.rebuild("Coins > 1000", "", "", true);

		assertEquals(ItemListSync.NONE, sync.hiddenOrHighlighted("Coins", 1000));
		assertEquals(ItemListSync.HIGHLIGHTED, sync.hiddenOrHighlighted("Coins", 1001));
	}

	@Test
	public void matchingIsCaseInsensitive()
	{
		ItemListSync sync = new ItemListSync();
		sync.rebuild("twisted BOW", "", "", true);

		assertEquals(ItemListSync.HIGHLIGHTED, sync.hiddenOrHighlighted("Twisted bow", 1));
	}
}
