package com.lootbeamsdeluxe;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OwnershipFilterTest
{
	@Test
	public void readsGroundItemsSetting()
	{
		assertEquals(OwnershipFilter.ALL, OwnershipFilter.fromGroundItems("ALL"));
		assertEquals(OwnershipFilter.DROPS, OwnershipFilter.fromGroundItems("DROPS"));
		assertEquals(OwnershipFilter.TAKEABLE, OwnershipFilter.fromGroundItems("TAKEABLE"));
	}

	@Test
	public void toleratesWhitespaceAndCase()
	{
		assertEquals(OwnershipFilter.DROPS, OwnershipFilter.fromGroundItems(" drops "));
	}

	@Test
	public void unsetOrUnknownFallsBackToGroundItemsDefault()
	{
		assertEquals(OwnershipFilter.ALL, OwnershipFilter.fromGroundItems(null));
		assertEquals(OwnershipFilter.ALL, OwnershipFilter.fromGroundItems(""));
		assertEquals(OwnershipFilter.ALL, OwnershipFilter.fromGroundItems("SOMETHING_NEW"));
	}

	@Test
	public void neverResolvesToSync()
	{
		assertEquals(OwnershipFilter.ALL, OwnershipFilter.fromGroundItems("SYNC"));
	}
}
