package com.stattrack;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(StatTrackConfig.GROUP)
public interface StatTrackConfig extends Config {
	String GROUP = "stattrack";

	@ConfigItem(
			keyName = "writeXp",
			name = "Log Experience Gains",
			description = "Log experience gains",
			position = 1
	)
	default boolean logXp() {
		return true;
	}
}
