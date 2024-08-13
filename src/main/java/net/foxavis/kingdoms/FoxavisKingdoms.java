package net.foxavis.kingdoms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.foxavis.kingdoms.entity.KingdomRank;
import net.foxavis.kingdoms.entity.locations.KingdomChunk;
import net.foxavis.kingdoms.util.CachedDataManager;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main class of this plugin, where everything starts up and shuts down
 * @author Kyomi
 */
public final class FoxavisKingdoms extends JavaPlugin {

	public static class Test {

		public KingdomRank rank;
		public String lmao;

		public Test(KingdomRank rank, String lmao) {
			this.rank = rank;
			this.lmao = lmao;
		}
	}

	private static Logger logger = null;
	public static Logger getLoggerInstance() { return logger; }

	private static Gson gson = null;
	public static Gson getGSON() { return gson; }

	@Override public void onEnable() {
		logger = getLogger();
		gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

		World world = getServer().getWorld("world");
		Location location = new Location(world, 0, 0, 0, 0, 0);
		logger.info(gson.toJson(location));
	}

	@Override public void onDisable() {
		CachedDataManager.getCaches().forEach(CachedDataManager::shutdown);
	}
}