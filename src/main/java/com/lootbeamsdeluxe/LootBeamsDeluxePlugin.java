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
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemLayer;
import net.runelite.api.Player;
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
import net.runelite.api.gameval.VarbitID;
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
	private static final String GROUND_ITEMS_OWNERSHIP_KEY = "ownershipFilterMode";
	private static final String PREVIEW_KEY_PREFIX = "preview";

	private static final String LEGACY_FANFARE_MODE_KEY = "fanfareMode";
	private static final String[] LEGACY_FANFARE_COLOR_KEYS = {"fanfareColor", "fanfareColor1", "fanfareColor2"};

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

	private WorldPoint previewTile;

	@Provides
	LootBeamsDeluxeConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LootBeamsDeluxeConfig.class);
	}

	@Override
	protected void startUp()
	{
		beamManager = new BeamManager(client, clientThread);
		migrateFanfareConfig();
		tierResolver = TierResolver.fromConfig(config, ownershipFilter());
		suppressFanfareUntilTick = client.getTickCount() + 1;
		clientThread.invoke(() ->
		{
			rebuild();
			restartPreviewBeam();
		});
	}

	@Override
	protected void shutDown()
	{
		clientThread.invokeLater(() -> beamManager.removeAll());
		trackedItems.clear();
		previewTile = null;
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
		if (ownGroup && event.getKey().startsWith(PREVIEW_KEY_PREFIX))
		{
			clientThread.invokeLater(this::restartPreviewBeam);
			return;
		}

		boolean syncedGroundItemsList = config.syncGroundItems()
			&& GROUND_ITEMS_GROUP.equals(event.getGroup())
			&& (GROUND_ITEMS_HIGHLIGHTED_KEY.equals(event.getKey()) || GROUND_ITEMS_HIDDEN_KEY.equals(event.getKey()));

		boolean syncedOwnership = config.ownershipFilter() == OwnershipFilter.SYNC
			&& GROUND_ITEMS_GROUP.equals(event.getGroup())
			&& GROUND_ITEMS_OWNERSHIP_KEY.equals(event.getKey());

		if (ownGroup || syncedGroundItemsList || syncedOwnership)
		{
			clientThread.invokeLater(this::rebuild);
		}
	}

	@Subscribe
	public void onProfileChanged(ProfileChanged event)
	{
		migrateFanfareConfig();
		clientThread.invokeLater(this::rebuild);
	}

	// fanfares used to be one global 'Play fanfare' mode naming the lowest tier
	// that got one — fold that into the per-tier dropdowns and drop the old keys
	private void migrateFanfareConfig()
	{
		String mode = configManager.getConfiguration(LootBeamsDeluxeConfig.GROUP, LEGACY_FANFARE_MODE_KEY);
		if (mode != null)
		{
			// OFF, HIGHLIGHTED_ONLY, or TIER_n
			int lowestTier = mode.startsWith("TIER_") ? Integer.parseInt(mode.substring(5)) : 9;
			for (int index = lowestTier; index <= 8; index++)
			{
				configManager.setConfiguration(LootBeamsDeluxeConfig.GROUP, "tier" + index + "Fanfare", FanfareOverride.DEFAULT);
			}

			if (!"OFF".equals(mode))
			{
				configManager.setConfiguration(LootBeamsDeluxeConfig.GROUP, "highlightFanfare", FanfareOverride.DEFAULT);
			}

			configManager.unsetConfiguration(LootBeamsDeluxeConfig.GROUP, LEGACY_FANFARE_MODE_KEY);
		}

		// custom fanfare colours are gone, they always follow the beam now
		for (String key : LEGACY_FANFARE_COLOR_KEYS)
		{
			configManager.unsetConfiguration(LootBeamsDeluxeConfig.GROUP, key);
		}
	}

	private void rebuild()
	{
		listSync.rebuild(
			configManager.getConfiguration(GROUND_ITEMS_GROUP, GROUND_ITEMS_HIGHLIGHTED_KEY),
			configManager.getConfiguration(GROUND_ITEMS_GROUP, GROUND_ITEMS_HIDDEN_KEY),
			config.additionalHighlights(),
			config.syncGroundItems());
		tierResolver = TierResolver.fromConfig(config, ownershipFilter());

		for (WorldPoint worldPoint : trackedItems.rowKeySet())
		{
			handleTile(worldPoint, false);
		}

		if (previewTile != null)
		{
			beamManager.handleTile(previewTile, previewResolution(), null);
		}
	}

	private void restartPreviewBeam()
	{
		if (previewTile != null)
		{
			beamManager.handleTile(previewTile, Resolution.NONE, null);
			previewTile = null;
		}

		Player player = client.getLocalPlayer();
		Resolution resolution = previewResolution();
		if (player == null || resolution.isBeamless())
		{
			return;
		}

		previewTile = config.previewDirection().from(player.getWorldLocation(), config.previewDistance());
		beamManager.handleTile(previewTile, resolution, fanfare(resolution));
	}

	// highlight wins outright and otherwise the highest ticked tier does, same as a real tile
	private Resolution previewResolution()
	{
		if (config.previewHighlight())
		{
			return tierResolver.highlight();
		}

		boolean[] ticked = {
			config.previewTier1(), config.previewTier2(), config.previewTier3(), config.previewTier4(),
			config.previewTier5(), config.previewTier6(), config.previewTier7(), config.previewTier8()};

		for (int index = ticked.length; index >= 1; index--)
		{
			if (ticked[index - 1])
			{
				return tierResolver.tier(index);
			}
		}

		return Resolution.NONE;
	}

	private OwnershipFilter ownershipFilter()
	{
		OwnershipFilter configured = config.ownershipFilter();
		return configured == OwnershipFilter.SYNC
			? OwnershipFilter.fromGroundItems(configManager.getConfiguration(GROUND_ITEMS_GROUP, GROUND_ITEMS_OWNERSHIP_KEY))
			: configured;
	}

	private void handleTile(WorldPoint worldPoint, boolean isNewSpawn)
	{
		Resolution resolution = tierResolver.resolve(trackedItems.row(worldPoint).values(), listSync,
			client.getVarbitValue(VarbitID.IRONMAN));
		boolean playFanfare = isNewSpawn && client.getTickCount() > suppressFanfareUntilTick;
		beamManager.handleTile(worldPoint, resolution, playFanfare ? fanfare(resolution) : null);
	}

	@Nullable
	private static Fanfare fanfare(Resolution resolution)
	{
		FanfareStyle style = resolution.getFanfare();
		return style == null ? null : new Fanfare(style, resolution.getPrimary(), resolution.getSecondary());
	}

	private TrackedItem buildTrackedItem(ItemLayer itemLayer, TileItem item)
	{
		final int itemId = item.getId();
		final ItemComposition itemComposition = itemManager.getItemComposition(itemId);
		final int realItemId = itemComposition.getNote() != -1 ? itemComposition.getLinkedNoteId() : itemId;

		long gePrice;
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
			.ownership(item.getOwnership())
			.build();
	}
}
