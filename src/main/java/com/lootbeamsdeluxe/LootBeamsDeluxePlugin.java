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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemLayer;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemQuantityChanged;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.WorldViewUnloaded;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Loot Beams Deluxe",
	description = "Loot beams with eight value tiers, two-tone colors, extra styles, and a drop fanfare effect. "
		+ "Disable Ground Items' built-in loot beams to avoid doubles.",
	tags = {"loot", "beam", "lootbeam", "drops", "ground", "items", "highlight"}
)
public class LootBeamsDeluxePlugin extends Plugin
{
	private static final String GROUND_ITEMS_GROUP = "grounditems";
	private static final String GROUND_ITEMS_HIGHLIGHTED_KEY = "highlightedItems";
	private static final String GROUND_ITEMS_HIDDEN_KEY = "hiddenItems";

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private LootBeamsDeluxeConfig config;

	private final Table<WorldPoint, Integer, TrackedItem> trackedItems = HashBasedTable.create();
	private final ItemListSync listSync = new ItemListSync();
	private BeamManager beamManager;
	private TierResolver tierResolver;

	// ItemSpawned also fires for stuff that was already on the ground (scene
	// loads, plugin startup) — only genuinely fresh drops should get a fanfare
	private int suppressFanfareUntilTick;

	@Provides
	LootBeamsDeluxeConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LootBeamsDeluxeConfig.class);
	}

	@Override
	protected void startUp()
	{
		beamManager = new BeamManager(client, clientThread);
		tierResolver = TierResolver.fromConfig(config);
		suppressFanfareUntilTick = client.getTickCount() + 1;
		clientThread.invoke(this::rebuild);
	}

	@Override
	protected void shutDown()
	{
		clientThread.invokeLater(() -> beamManager.removeAll());
		trackedItems.clear();
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		TileItem item = itemSpawned.getItem();
		Tile tile = itemSpawned.getTile();
		WorldPoint worldPoint = tile.getWorldLocation();

		TrackedItem trackedItem = buildTrackedItem(tile.getItemLayer(), item);
		TrackedItem existing = trackedItems.get(worldPoint, item.getId());
		if (existing != null)
		{
			existing.setQuantity(existing.getQuantity() + trackedItem.getQuantity());
		}
		else
		{
			trackedItems.put(worldPoint, item.getId(), trackedItem);
		}

		handleTile(worldPoint, true);
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned itemDespawned)
	{
		TileItem item = itemDespawned.getItem();
		WorldPoint worldPoint = itemDespawned.getTile().getWorldLocation();

		TrackedItem trackedItem = trackedItems.get(worldPoint, item.getId());
		if (trackedItem == null)
		{
			return;
		}

		if (trackedItem.getQuantity() <= item.getQuantity())
		{
			trackedItems.remove(worldPoint, item.getId());
		}
		else
		{
			trackedItem.setQuantity(trackedItem.getQuantity() - item.getQuantity());
		}

		handleTile(worldPoint, false);
	}

	@Subscribe
	public void onItemQuantityChanged(ItemQuantityChanged itemQuantityChanged)
	{
		TileItem item = itemQuantityChanged.getItem();
		WorldPoint worldPoint = itemQuantityChanged.getTile().getWorldLocation();
		int diff = itemQuantityChanged.getNewQuantity() - itemQuantityChanged.getOldQuantity();

		TrackedItem trackedItem = trackedItems.get(worldPoint, item.getId());
		if (trackedItem != null)
		{
			trackedItem.setQuantity(trackedItem.getQuantity() + diff);
		}

		handleTile(worldPoint, false);
	}

	@Subscribe
	public void onWorldViewUnloaded(WorldViewUnloaded event)
	{
		int worldViewId = event.getWorldView().getId();
		trackedItems.values().removeIf(item -> item.getWorldViewId() == worldViewId);
		beamManager.removeWorldView(worldViewId);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING)
		{
			suppressFanfareUntilTick = client.getTickCount() + 1;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		beamManager.tick();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		boolean ownGroup = LootBeamsDeluxeConfig.GROUP.equals(event.getGroup());
		boolean syncedGroundItemsList = config.syncGroundItems()
			&& GROUND_ITEMS_GROUP.equals(event.getGroup())
			&& (GROUND_ITEMS_HIGHLIGHTED_KEY.equals(event.getKey()) || GROUND_ITEMS_HIDDEN_KEY.equals(event.getKey()));

		if (ownGroup || syncedGroundItemsList)
		{
			clientThread.invokeLater(this::rebuild);
		}
	}

	@Subscribe
	public void onProfileChanged(ProfileChanged event)
	{
		clientThread.invokeLater(this::rebuild);
	}

	private void rebuild()
	{
		listSync.rebuild(
			configManager.getConfiguration(GROUND_ITEMS_GROUP, GROUND_ITEMS_HIGHLIGHTED_KEY),
			configManager.getConfiguration(GROUND_ITEMS_GROUP, GROUND_ITEMS_HIDDEN_KEY),
			config.additionalHighlights(),
			config.syncGroundItems());
		tierResolver = TierResolver.fromConfig(config);

		for (WorldPoint worldPoint : trackedItems.rowKeySet())
		{
			handleTile(worldPoint, false);
		}
	}

	private void handleTile(WorldPoint worldPoint, boolean isNewSpawn)
	{
		Resolution resolution = tierResolver.resolve(trackedItems.row(worldPoint).values(), listSync);
		boolean playFanfare = isNewSpawn
			&& client.getTickCount() > suppressFanfareUntilTick
			&& config.fanfareMode().qualifies(resolution);
		beamManager.handleTile(worldPoint, resolution, playFanfare ? fanfare(resolution) : null);
	}

	// colours resolved here because 'Match beam' needs the tier's palette
	private Fanfare fanfare(Resolution resolution)
	{
		if (config.fanfareColor() == FanfareColor.CUSTOM)
		{
			return new Fanfare(config.fanfareStyle(), config.fanfareColor1(), config.fanfareColor2());
		}
		return new Fanfare(config.fanfareStyle(), resolution.getPrimary(), resolution.getSecondary());
	}

	private TrackedItem buildTrackedItem(ItemLayer itemLayer, TileItem item)
	{
		final int itemId = item.getId();
		final ItemComposition itemComposition = itemManager.getItemComposition(itemId);
		final int realItemId = itemComposition.getNote() != -1 ? itemComposition.getLinkedNoteId() : itemId;

		int gePrice;
		int haPrice;
		if (realItemId == ItemID.COINS)
		{
			gePrice = 1;
			haPrice = 1;
		}
		else
		{
			gePrice = itemManager.getItemPrice(realItemId);
			haPrice = itemComposition.getHaPrice();
		}

		return TrackedItem.builder()
			.itemId(itemId)
			.name(itemComposition.getName())
			.quantity(item.getQuantity())
			.gePrice(gePrice)
			.haPrice(haPrice)
			.worldViewId(itemLayer.getWorldView().getId())
			.build();
	}
}
