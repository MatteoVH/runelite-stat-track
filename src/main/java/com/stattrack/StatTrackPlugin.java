package com.stattrack;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import net.runelite.client.account.AccountSession;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.EnumMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;


@PluginDescriptor(
		name = "Stat Track",
		description = "Tracks stats to compare with your friends",
		tags = {"experience", "levels", "stats", "activity", "tracker", "group", "ironman"}
)
@Slf4j
public class StatTrackPlugin extends Plugin {
	@Provides
	StatTrackConfig getConfig(ConfigManager configManager) {
		return configManager.getConfig(StatTrackConfig.class);
	}

	@Inject
	private ConfigManager configManager;

	@Inject
	private StatTrackConfig config;

	@Inject
	private Client client;

	private final EnumMap<Skill, Integer> previousStatXp = new EnumMap<>(Skill.class);

	private final OkHttpClient httpClient;

	@Inject
	public StatTrackPlugin() {
		this.httpClient = new OkHttpClient();
	}

	public static final MediaType JSON
			= MediaType.get("application/json; charset=utf-8");

	@Subscribe
	public void onStatChanged(StatChanged statChanged) {
		log.debug("stat changed %s", statChanged);
		log.debug("current username %s", client.getLocalPlayer().getName());
		if (statChanged.getXp() == 0 || client.getGameState() != GameState.LOGGED_IN)
			return;
		final Integer previous = previousStatXp.put(statChanged.getSkill(), statChanged.getXp());
		if (previous == null || previous == statChanged.getXp())
			return;
		previousStatXp.put(statChanged.getSkill(), statChanged.getXp());

		if (config.logXp()) {
			Skill skill = statChanged.getSkill();
			long xp = skill == Skill.OVERALL ? client.getOverallExperience() : client.getSkillExperience(skill);
			if (xp != 0) {
				log.debug("about to write a test doc to MongoDB");
				logXp(statChanged.getSkill(), statChanged.getLevel(), statChanged.getXp(), client.getLocalPlayer().getName());
			}
		}
	}

	public final void logXp(Skill skill, int level, int xp, String playerUsername) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("skill", skill.getName());
		jsonObject.addProperty("level", level);
		jsonObject.addProperty("xp", xp);
		jsonObject.addProperty("username", playerUsername);

		Gson gson = new Gson();
		String stringifiedJson = gson.toJson(jsonObject);
		log.debug(stringifiedJson);

		RequestBody body = RequestBody.create(JSON, stringifiedJson);

		String url = "https://osrs-group-ironman-stats.vercel.app/api/logXp";
		String localUrl = "http://localhost:3000/api/logXp";
		Request request = new Request.Builder().url(url).post(body).build();
		try {
			Response response = httpClient.newCall(request).execute();
			log.debug(response.toString());
		} catch (IOException e) {
			log.debug(e.toString());
			throw new RuntimeException(e);
		}
	}
}
