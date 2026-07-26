package com.lootbeamsdeluxe;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ItemThresholdTest
{
	@Test
	public void plainNameMatchesAnyQuantity()
	{
		ItemThreshold t = ItemThreshold.fromName("Shark");
		assertEquals("Shark", t.getName());
		assertFalse(t.isWildcard());
		assertTrue(t.quantityHolds(1));
		assertTrue(t.quantityHolds(1000));
	}

	@Test
	public void moreThanThreshold()
	{
		ItemThreshold t = ItemThreshold.fromName("Coins > 500");
		assertEquals("Coins", t.getName());
		assertFalse(t.quantityHolds(500));
		assertTrue(t.quantityHolds(501));
	}

	@Test
	public void lessThanThreshold()
	{
		ItemThreshold t = ItemThreshold.fromName("Feather<20");
		assertEquals("Feather", t.getName());
		assertTrue(t.quantityHolds(19));
		assertFalse(t.quantityHolds(20));
	}

	@Test
	public void wildcardIsDetected()
	{
		ItemThreshold t = ItemThreshold.fromName("Rune *");
		assertTrue(t.isWildcard());
		assertEquals("Rune *", t.getName());
	}

	@Test
	public void emptyEntryIsNull()
	{
		assertNull(ItemThreshold.fromName(""));
		assertNull(ItemThreshold.fromName(null));
	}

	@Test
	public void malformedQuantityFallsBack()
	{
		ItemThreshold t = ItemThreshold.fromName("Bones>x1");
		assertEquals(0, t.getQuantity());
		assertTrue(t.quantityHolds(1));
	}
}
