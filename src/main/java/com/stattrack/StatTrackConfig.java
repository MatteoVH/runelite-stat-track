package com.stattrack;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(StatTrackConfig.GROUP)
public interface StatTrackConfig extends Config {
	String GROUP = "stattrack";

	@ConfigItem(
			keyName = "mongodbUri",
			name = "MongoDB URI",
			description = "URL of the MongoDB instance to write to",
			position = 0
	)
	String getMongodbUri();

	@ConfigItem(
			keyName = "writeXp",
			name = "Submit Experience",
			description = "Submit experience amount",
			position = 5
	)
	default boolean writeXp() {
		return true;
	}
}
