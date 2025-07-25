/*
 * Copyright (c) 2018, Lotto <https://github.com/devLotto>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.lastman;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
		name = "Last Man on Gielinor",
		description = "Created for YT Series, The Last Man on Gielinor, this plugin adds the ability to make NPCs visible based on ID or interaction options.",
		tags = {"npcs"},
		enabledByDefault = false
)
public class LastManPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private LastManConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Hooks hooks;

	private boolean hideTalkNPCs;

	private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;

	private final Set<Integer> npcBlacklist = new HashSet<>();
	private final Set<Integer> npcWhitelist = new HashSet<>();

	@Provides
	LastManConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LastManConfig.class);
	}

	@Override
	protected void startUp()
	{
		updateConfig();
		hooks.registerRenderableDrawListener(drawListener);
	}

	@Override
	protected void shutDown()
	{
		hooks.unregisterRenderableDrawListener(drawListener);
		npcBlacklist.clear();
		npcWhitelist.clear();
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!config.showMenuOptions())
		{
			return;
		}

		if (event.getType() != MenuAction.NPC_FIRST_OPTION.getId()
				&& event.getType() != MenuAction.NPC_SECOND_OPTION.getId())
		{
			return;
		}

		final int npcIndex = event.getIdentifier();
		final NPC npc = client.getNpcs().stream()
				.filter(n -> n.getIndex() == npcIndex)
				.findFirst()
				.orElse(null);

		if (npc == null || npc.getTransformedComposition() == null)
			return;

		final int npcId = npc.getTransformedComposition().getId();
		final String npcName = npc.getName();

		final MenuEntry[] entries = client.getMenuEntries();
		boolean alreadyHasBlacklist = false;
		boolean alreadyHasWhitelist = false;

		for (MenuEntry entry : entries)
		{
			if (entry.getIdentifier() == npcId)
			{
				final String option = Text.removeTags(entry.getOption());

				if (option.equalsIgnoreCase("(+) Blacklist") || option.equalsIgnoreCase("Blacklist (-)"))
					alreadyHasBlacklist = true;
				if (option.equalsIgnoreCase("(+) Whitelist") || option.equalsIgnoreCase("Whitelist (-)"))
					alreadyHasWhitelist = true;
			}
		}

		if (!alreadyHasBlacklist)
		{
			client.createMenuEntry(-1)
					.setOption(npcBlacklist.contains(npcId) ? "Blacklist (-)" : "(+) Blacklist")
					.setTarget(npcName)
					.setType(MenuAction.RUNELITE)
					.setIdentifier(npcId);
		}

		if (!alreadyHasWhitelist)
		{
			client.createMenuEntry(-1)
					.setOption(npcWhitelist.contains(npcId) ? "Whitelist (-)" : "(+) Whitelist")
					.setTarget(npcName)
					.setType(MenuAction.RUNELITE)
					.setIdentifier(npcId);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		String option = Text.removeTags(event.getMenuOption());
		int npcId = event.getId();

		switch (option)
		{
			case "(+) Blacklist":
				npcBlacklist.add(npcId);
				updateConfigBlacklist();
				event.consume();
				break;
			case "Blacklist (-)":
				npcBlacklist.remove(npcId);
				updateConfigBlacklist();
				event.consume();
				break;
			case "(+) Whitelist":
				npcWhitelist.add(npcId);
				updateConfigWhitelist();
				event.consume();
				break;
			case "Whitelist (-)":
				npcWhitelist.remove(npcId);
				updateConfigWhitelist();
				event.consume();
				break;
		}
	}

	private void updateConfigBlacklist()
	{
		String value = npcBlacklist.stream()
				.map(String::valueOf)
				.collect(Collectors.joining(","));
		configManager.setConfiguration(LastManConfig.GROUP, "npcBlacklist", value);
	}

	private void updateConfigWhitelist()
	{
		String value = npcWhitelist.stream()
				.map(String::valueOf)
				.collect(Collectors.joining(","));
		configManager.setConfiguration(LastManConfig.GROUP, "npcWhitelist", value);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (e.getGroup().equals(LastManConfig.GROUP))
		{
			updateConfig();
		}
	}

	private void updateConfig()
	{
		hideTalkNPCs = config.hideTalkNPCs();
		npcBlacklist.clear();
		npcWhitelist.clear();

		for (String id : config.npcBlacklist().split(","))
		{
			try
			{
				npcBlacklist.add(Integer.parseInt(id.trim()));
			}
			catch (NumberFormatException ignored) {}
		}

		for (String id : config.npcWhitelist().split(","))
		{
			try
			{
				npcWhitelist.add(Integer.parseInt(id.trim()));
			}
			catch (NumberFormatException ignored) {}
		}
	}

	@VisibleForTesting
	boolean shouldDraw(Renderable renderable, boolean drawingUI)
	{
		if (!(renderable instanceof NPC))
			return true;

		NPC npc = (NPC) renderable;
		NPCComposition comp = npc.getTransformedComposition();
		if (comp == null)
			return true;

		int id = comp.getId();

		if (config.enableWhitelist() && npcWhitelist.contains(id))
			return true;

		if (config.enableBlacklist() && npcBlacklist.contains(id))
			return false;

		return !hideTalkNPCs || !isTalkable(npc);
	}

	private boolean isTalkable(NPC npc)
	{
		NPCComposition composition = npc.getTransformedComposition();
		if (composition == null)
			return false;

		String[] actions = composition.getActions();
		if (actions == null)
			return false;

		for (String action : actions)
		{
			if ("Talk-to".equalsIgnoreCase(action))
				return true;
		}
		return false;
	}
}
