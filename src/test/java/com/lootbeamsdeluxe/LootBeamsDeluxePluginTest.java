package com.lootbeamsdeluxe;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LootBeamsDeluxePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(LootBeamsDeluxePlugin.class);
		RuneLite.main(args);
	}
}
