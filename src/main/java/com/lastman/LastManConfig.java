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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(LastManConfig.GROUP)
public interface LastManConfig extends Config
{
	String GROUP = "lastman";

	@ConfigItem(
			position = 0,
			keyName = "hideTalkNPCs",
			name = "Hide Talkable NPCs",
			description = "Hide all NPCs with the 'Talk-to' interaction."
	)
	default boolean hideTalkNPCs()
	{
		return false;
	}

	@ConfigItem(
			position = 1,
			keyName = "enableWhitelist",
			name = "Enable Whitelist",
			description = "Whitelisted NPCs will always be visible."
	)
	default boolean enableWhitelist()
	{
		return true;
	}

	@ConfigItem(
			position = 2,
			keyName = "enableBlacklist",
			name = "Enable Blacklist",
			description = "Blacklisted NPCs will always be hidden."
	)
	default boolean enableBlacklist()
	{
		return true;
	}

	@ConfigItem(
			position = 3,
			keyName = "showMenuOptions",
			name = "Show NPC Menu Options",
			description = "Toggle visibility of context menu actions."
	)
	default boolean showMenuOptions()
	{
		return true;
	}

	@ConfigItem(
			position = 4,
			keyName = "npcWhitelist",
			name = "NPC Whitelist",
			description = "Comma-separated list of NPC IDs to whitelist",
			hidden = true
	)
	default String npcWhitelist()
	{
		return "";
	}

	@ConfigItem(
			position = 5,
			keyName = "npcBlacklist",
			name = "NPC Blacklist",
			description = "Comma-separated list of NPC IDs to blacklist",
			hidden = true
	)
	default String npcBlacklist()
	{
		return "";
	}
}
