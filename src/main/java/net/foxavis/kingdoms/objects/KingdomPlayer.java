package net.foxavis.kingdoms.objects;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.util.CachedDataManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class KingdomPlayer {

	// -- Constants -- \\
	public static final double DEFAULT_POWER = 5.0;
	public static final double DEFAULT_POWER_BOOST = 1.0;

	public static final double MAX_POWER = 10.0;

	public static final double POWER_INCREMENT = 2;
	public static final long POWER_INCREMENT_TIME = 1; // in hours

	public static final double POWER_DECREMENT = 2; // per kill

	// -- Static -- \\
	public enum ChatType { ALLY, KINGDOM, GLOBAL }
	public static Map<UUID, ChatType> chatTypes = new HashMap<>();

	public static ChatType getChatType(@NotNull Player player) {
		return chatTypes.getOrDefault(player.getUniqueId(), ChatType.GLOBAL);
	}

	public static void setChatType(@NotNull Player player, @Nullable ChatType chatType) {
		if(chatType == null) chatTypes.remove(player.getUniqueId());
		else chatTypes.put(player.getUniqueId(), chatType);
	}

	private static File getPlayersFolder() {
		File pluginDataFolder = FoxavisKingdoms.getInstance().getDataFolder();
		File kingdomFolder = new File(pluginDataFolder, "players");

		if (!kingdomFolder.exists()) {
			if (!kingdomFolder.mkdirs()) {
				throw new RuntimeException("Failed to create players folder.");
			}
		}

		return kingdomFolder;
	}

	private static final CachedDataManager<UUID, KingdomPlayer> cache = new CachedDataManager<>(10, TimeUnit.MINUTES) {
		@Override protected @Nullable KingdomPlayer loadFromSource(@NotNull UUID key) {
			File file = new File(getPlayersFolder(), key.toString() + ".json");
			if (!file.exists()) return null;

			return FoxavisKingdoms.getGSON().fromJson(CachedDataManager.readFile(file), KingdomPlayer.class);
		}

		@Override protected void saveToSource(@NotNull UUID key, @NotNull KingdomPlayer value) {
			File file = new File(getPlayersFolder(), key.toString() + ".json");
			CachedDataManager.writeToFile(file, FoxavisKingdoms.getGSON().toJson(value));
		}

		@Override protected void deleteFromSource(@NotNull UUID key) {
			File file = new File(getPlayersFolder(), key.toString() + ".json");
			if (file.exists())
				if (!file.delete())
					throw new RuntimeException("Failed to delete player file.");
		}
	};

	@Nullable public static KingdomPlayer fetchPlayer(@NotNull OfflinePlayer player) {
		return cache.getData(player.getUniqueId());
	}

	// -- Fields -- \\
	private final UUID playerId; // The UUID of the player
	private final String playerName; // The name of the player

	@Nullable private UUID kingdomId; // The UUID of the kingdom the player is in

	private double power; // The power of the player
	private double powerBoost; // The power boost of the player (must be 0.xx, 1.0 is default)
	private long lastPowerIncrement; // The last time the player's power was incremented

	private boolean territoryInfoNotifs; // Whether the player wants territory info notifications
	private boolean isOverriding; // Whether the player is bypassing territory restrictions (admins only)
	private boolean isSocialSpying; // Whether the player is social spying (admins only)
	private transient boolean isAutoClaiming; // Whether the player is auto claiming territory

	@Nullable private Long lastOnline; // The last time the player was online

	// -- Constructors -- \\
	public KingdomPlayer(@NotNull Player player) {
		this.playerId = player.getUniqueId();
		this.playerName = player.getName();

		this.kingdomId = null;

		this.power = DEFAULT_POWER;
		this.powerBoost = DEFAULT_POWER_BOOST;
		this.lastPowerIncrement = System.currentTimeMillis();

		this.territoryInfoNotifs = true;
		this.isOverriding = false;
		this.isSocialSpying = false;
		this.isAutoClaiming = false;

		// if null, the player has never left the server
		this.lastOnline = null;

		cache.setData(playerId, this);
	}

	// -- Getters -- \\
	public UUID getPlayerId() { return playerId; }
	public OfflinePlayer fetchPlayer() { return Bukkit.getOfflinePlayer(playerId); }
	public String getPlayerName() { return playerName; }

	@Nullable public UUID getKingdomId() { return kingdomId; }

	public double getPowerBoost() { return powerBoost; }
	public double getPower() { return power * powerBoost; }
	public long getLastPowerIncrement() { return lastPowerIncrement; }

	public double getMaxPower() { return MAX_POWER * powerBoost; }
	public double getPowerGain() { return POWER_INCREMENT * powerBoost; }
	public double getPowerLoss() { return POWER_DECREMENT; }

	public boolean wantsTerritoryInfoNotifs() { return territoryInfoNotifs; }
	public boolean isOverriding() { return isOverriding; }
	public boolean isSocialSpying() { return isSocialSpying; }
	public boolean isAutoClaiming() { return isAutoClaiming; }

	@Nullable public Long getLastOnline() { return lastOnline; }

	// -- Setters -- \\
	public void setKingdomId(@Nullable UUID kingdomId) { this.kingdomId = kingdomId; }

	public void setPowerBoost(double powerBoost) { this.powerBoost = powerBoost; }
	public void setPower(double power) { this.power = power; }
	public void setLastPowerIncrement(long lastPowerIncrement) { this.lastPowerIncrement = lastPowerIncrement; }

	public void setTerritoryInfoNotifs(boolean territoryInfoNotifs) { this.territoryInfoNotifs = territoryInfoNotifs; }
	public void setOverriding(boolean isOverriding) { this.isOverriding = isOverriding; }
	public void setSocialSpying(boolean isSocialSpying) { this.isSocialSpying = isSocialSpying; }
	public void setAutoClaiming(boolean isAutoClaiming) { this.isAutoClaiming = isAutoClaiming; }

	public void setLastOnline(@Nullable Long lastOnline) { this.lastOnline = lastOnline; }

	// -- Methods -- \\
	public void incrementPower() {
		long currentTime = System.currentTimeMillis();
		long timeDifference = currentTime - lastPowerIncrement;

		if (timeDifference >= TimeUnit.HOURS.toMillis(POWER_INCREMENT_TIME)) {
			lastPowerIncrement = currentTime;
			power += POWER_INCREMENT;
		}
	}

	public long getTimeTillNextPowerIncrement() {
		long currentTime = System.currentTimeMillis();
		long timeDifference = currentTime - lastPowerIncrement;

		return TimeUnit.HOURS.toMillis(POWER_INCREMENT_TIME) - timeDifference;
	}

	public String formatTTNPI() {
		long time = getTimeTillNextPowerIncrement();

		long hours = TimeUnit.MILLISECONDS.toHours(time);
		time -= TimeUnit.HOURS.toMillis(hours);

		long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
		time -= TimeUnit.MINUTES.toMillis(minutes);

		long seconds = TimeUnit.MILLISECONDS.toSeconds(time);
		time -= TimeUnit.SECONDS.toMillis(seconds);

		DecimalFormat format = new DecimalFormat("00");

		return format.format(hours) + ":" + format.format(minutes) + ":" + format.format(seconds);
	}

	public void save() {
		cache.setData(playerId, this);
	}

	public void delete() {
		cache.deleteData(playerId);
	}
}
