package net.foxavis.kingdoms.objects.kingdoms;

import com.google.gson.reflect.TypeToken;
import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.enums.KingdomFlags;
import net.foxavis.kingdoms.enums.KingdomPerms;
import net.foxavis.kingdoms.enums.KingdomRank;
import net.foxavis.kingdoms.enums.KingdomRelation;
import net.foxavis.kingdoms.objects.KingdomPlayer;
import net.foxavis.kingdoms.util.CachedDataManager;
import net.foxavis.kingdoms.util.KingdomException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Kingdom {

	// --- Static --- \\
	// -- Constants -- \\
	public static final int MAX_NAME_LENGTH = 32; // The maximum length of the kingdom name.
	public static final int MAX_DESCRIPTION_LENGTH = 48; // The maximum length of the kingdom description.
	public static final int MAX_MOTD_LENGTH = 512; // The maximum length of the kingdom motd.
	public static final int MAX_TAG_LENGTH = 5; // The maximum length of the kingdom tag.

	public static final long InviteTimeout = TimeUnit.DAYS.toMillis(3); // The time in milliseconds before an invitation expires.

	@ApiStatus.Internal
	public static NamespacedKey getPDCKingdomKey() { return new NamespacedKey(FoxavisKingdoms.getInstance(), "kingdom.id"); }

	// -- Methods -- \\
	private static File getKingdomFolder() {
		File pluginDataFolder = FoxavisKingdoms.getInstance().getDataFolder();
		File kingdomFolder = new File(pluginDataFolder, "kingdoms");

		if (!kingdomFolder.exists()) {
			if (!kingdomFolder.mkdirs()) {
				throw new RuntimeException("Failed to create kingdom folder.");
			}
		}

		return kingdomFolder;
	}

	private static Map<UUID, String> kingdomIndex = null;
	public static Map<UUID, String> getKingdomIndex() {
		if(kingdomIndex == null) {
			try {
				File file = new File(getKingdomFolder(), "index.json");
				if (file.createNewFile()) {
					try (FileWriter writer = new FileWriter(file)) {
						writer.write("{}");
					}
				}

				kingdomIndex = FoxavisKingdoms.getGSON().fromJson(CachedDataManager.readFile(file), new TypeToken<Map<UUID, String>>(){}.getType());
			} catch (IOException e) {
				throw new RuntimeException("Failed to create kingdom index file.", e);
			}
		}

		return kingdomIndex;
	}

	public static void saveKingdomIndex() {
		File file = new File(getKingdomFolder(), "index.json");

		if(kingdomIndex == null) getKingdomIndex();
		CachedDataManager.writeToFile(file, FoxavisKingdoms.getGSON().toJson(kingdomIndex));
	}

	private static final CachedDataManager<UUID, Kingdom> cache = new CachedDataManager<>(10, TimeUnit.MINUTES) {
		@Override protected @Nullable Kingdom loadFromSource(@NotNull UUID key) {
			File file = new File(getKingdomFolder(), key + ".json");
			if (!file.exists()) return null;

			return FoxavisKingdoms.getGSON().fromJson(CachedDataManager.readFile(file), Kingdom.class);
		}

		@Override protected void saveToSource(@NotNull UUID key, @NotNull Kingdom value) {
			File file = new File(getKingdomFolder(), key + ".json");
			CachedDataManager.writeToFile(file, FoxavisKingdoms.getGSON().toJson(value));
		}

		@Override protected void deleteFromSource(@NotNull UUID key) {
			File file = new File(getKingdomFolder(), key + ".json");
			if (file.exists())
				if(!file.delete())
					throw new RuntimeException("Failed to delete kingdom file.");
		}
	};

	/**
	 * Fetches a kingdom using it's unique identifier from the file system.
	 * @param kingdomId The unique identifier of the kingdom.
	 * @return The kingdom object, or null if the kingdom does not exist.
	 */
	@Nullable public static Kingdom fetchKingdom(@Nullable UUID kingdomId) {
		if(kingdomId == null) return null;
		return cache.getData(kingdomId);
	}

	/**
	 * Fetches a kingdom using a name.
	 * @param kingdomName The name of the potential kingdom.
	 * @return The kingdom object, or null if the kingdom does not exist.
	 */
	@Nullable public static Kingdom fetchKingdom(String kingdomName) {
		return fetchKingdom(getKingdomIndex().entrySet().stream()
				.filter(e -> e.getValue().equals(kingdomName))
				.findFirst()
				.map(Map.Entry::getKey)
				.orElse(null)
		);
	}

	@Nullable public static Kingdom fetchKingdom(Player player) {
		String data = player.getPersistentDataContainer().get(getPDCKingdomKey(), PersistentDataType.STRING);
		if (data == null) return null;

		return fetchKingdom(UUID.fromString(data));
	}

	@Nullable public static Kingdom fetchKingdom(Chunk chunk) {
		String data = chunk.getPersistentDataContainer().get(getPDCKingdomKey(), PersistentDataType.STRING);
		if (data == null) return null;

		return fetchKingdom(UUID.fromString(data));
	}

	@Nullable public static Kingdom fetchKingdom(Location location) {
		String data = location.getChunk().getPersistentDataContainer().get(getPDCKingdomKey(), PersistentDataType.STRING);
		if (data == null) return null;

		return fetchKingdom(UUID.fromString(data));
	}

	public static boolean doesKingdomExist(String kingdomName) {
		Kingdom kingdom = fetchKingdom(kingdomName);
		return kingdom != null;
	}

	public static boolean doesKingdomExist(UUID kingdomId) {
		Kingdom kingdom = fetchKingdom(kingdomId);
		return kingdom != null;
	}

	public static void sendSocialSpyMessage(Kingdom kingdom, Player from, ComponentLike component, boolean toAllies) {
		for(Player op : Bukkit.getOnlinePlayers()) {
			KingdomPlayer kPlayer = KingdomPlayer.fetchPlayer(op);
			if(kPlayer == null) kPlayer = new KingdomPlayer(op);

			if(!kPlayer.isSocialSpying()) continue;
			op.sendMessage(Component.text("[KSC] ").color(NamedTextColor.DARK_AQUA)
							.append((toAllies ? Component.text("[Allies] ").color(NamedTextColor.AQUA) : Component.text()))
							.append(Component.text("[").color(NamedTextColor.GRAY))
							.append(Component.text(kingdom.getName()).color(NamedTextColor.YELLOW))
							.append(Component.text("] ").color(NamedTextColor.GRAY))
							.append(Component.text(from.getName()).color(NamedTextColor.GOLD))
							.append(Component.text(" » ").color(NamedTextColor.GRAY))
							.append(Component.text().color(NamedTextColor.WHITE).append(component))
					);
		}
	}

	// --- Fields --- \\
	// -- Indentifiers -- \\
	private UUID kingdomId; // PK: The unique identifier of the kingdom.

	// -- Display -- \\
	private String name; // the name of the kingdom
	@Nullable private String description; // Optional description of the kingdom.
	@Nullable private String motd; // When members log in, they will see this message.
	private String tag; // The tag, or prefix, of the kingdom. Formatted like [<tag_value>]

	private final long timeCreated; // The time the kingdom was created.

	// -- Members -- \\
	private final Map<UUID, KingdomRank> members; // The members of the kingdom. (The player, their rank)
	private final Map<UUID, Long> invites; // The players who have been invited to the kingdom. (The player, when invited)

	// -- Territory -- \\
	private final List<KingdomChunk> territory; // The territory of the kingdom.
	@Nullable private KingdomLocation home; // The home location of the kingdom.
	private final Map<String, KingdomLocation> warps; // The warps of the kingdom. (The name of the warp, the location of the warp)

	// -- Money -- \\
	private double balance; // The balance of the kingdom.

	// -- Misc (maps) -- \\
	private final Map<KingdomRank, List<KingdomPerms>> rankPerms; // The permissions of the kingdom. (The rank, the permissions of the rank)
	private final Map<KingdomFlags, Boolean> flags; // The flags of the kingdom. (The flag, the value of the flag)
	private final Map<UUID, KingdomRelation> relations; // The relations of the kingdom. (The kingdom id, the relation)

	private boolean isPersistent; // Whether a kingdom stays despite having no members.
	private double powerBoost; // The power boost of the kingdom.

	// --- Constructors --- \\
	public Kingdom(Player leader, @NotNull String name) throws KingdomException {
		if(name.isBlank() || name.isEmpty()) throw new KingdomException("The kingdom name cannot be empty");
		if(name.length() > MAX_NAME_LENGTH) throw new KingdomException("The kingdom name cannot be longer than " + MAX_NAME_LENGTH + " characters");
		if(doesKingdomExist(name)) throw new KingdomException("A kingdom with the name " + name + " already exists");

		do this.kingdomId = UUID.randomUUID(); while (doesKingdomExist(kingdomId));

		this.name = name;
		this.description = null;
		this.motd = null;
		setTag(Component.text(name.substring(0, Math.min(name.length(), MAX_TAG_LENGTH))).color(NamedTextColor.GRAY));

		this.timeCreated = System.currentTimeMillis();

		this.members = new HashMap<>() {{ put(leader.getUniqueId(), KingdomRank.LEADER); }};
		this.invites = new HashMap<>();

		this.territory = new ArrayList<>();
		this.home = null;
		this.warps = new HashMap<>();

		this.balance = 0;

		this.rankPerms = KingdomRank.Default;
		this.flags = KingdomFlags.Default;
		this.relations = new HashMap<>();

		isPersistent = false;
		powerBoost = 1.0;

		leader.getPersistentDataContainer().set(getPDCKingdomKey(), PersistentDataType.STRING, kingdomId.toString());

		cache.setData(kingdomId, this);
		kingdomIndex.put(kingdomId, name);
	}

	// --- Methods --- \\
	// -- Getters -- \\
	// - Indentifiers - \\
	public UUID getKingdomId() { return kingdomId; }

	// - Display - \\
	public String getName() { return name; }
	public String getDescription() { return description; }
	public @Nullable String getMotd() { return motd; }
	public TextComponent getTag() { return LegacyComponentSerializer.legacyAmpersand().deserialize(tag); }

	public long getTimeCreated() { return timeCreated; }

	// - Members - \\
	public Map<UUID, KingdomRank> getMembers() { return members; }

	public UUID getLeaderId() { return members.entrySet().stream().filter(e -> e.getValue() == KingdomRank.LEADER).findFirst().map(Map.Entry::getKey).orElse(null); }
	public OfflinePlayer getLeader() { return Bukkit.getOfflinePlayer(getLeaderId()); }

	public boolean isLeader(UUID query) { return members.get(query) == KingdomRank.LEADER; }
	public boolean isLeader(OfflinePlayer query) { return isLeader(query.getUniqueId()); }

	public List<UUID> getAllMemberIds() { return new ArrayList<>(members.keySet()); }
	public List<OfflinePlayer> getAllMembers() { return members.keySet().stream().map(Bukkit::getOfflinePlayer).toList(); }
	public List<Player> getAllOnlineMembers() { return getAllMembers().stream().filter(OfflinePlayer::isOnline).map(OfflinePlayer::getPlayer).toList(); }

	public boolean isMember(UUID query) { return members.containsKey(query); }
	public boolean isMember(OfflinePlayer query) { return isMember(query.getUniqueId()); }

	public List<UUID> getMemberIdsByRank(KingdomRank rank) { return members.entrySet().stream().filter(e -> e.getValue() == rank).map(Map.Entry::getKey).toList(); }
	public List<OfflinePlayer> getMembersByRank(KingdomRank rank) { return getMemberIdsByRank(rank).stream().map(Bukkit::getOfflinePlayer).toList(); }
	public List<Player> getOnlineMembersByRank(KingdomRank rank) { return getMembersByRank(rank).stream().filter(OfflinePlayer::isOnline).map(OfflinePlayer::getPlayer).toList(); }

	public boolean hasRank(UUID query, KingdomRank rank) { return members.get(query) == rank; }
	public boolean hasRank(OfflinePlayer query, KingdomRank rank) { return hasRank(query.getUniqueId(), rank); }

	@Nullable public KingdomRank getRank(@NotNull UUID query) { return members.getOrDefault(query, null); }
	@Nullable public KingdomRank getRank(@NotNull OfflinePlayer query) { return getRank(query.getUniqueId()); }

	public Map<UUID, Long> getInvites() { return invites; }
	public boolean isInvited(UUID query) { return invites.containsKey(query); }
	public boolean isInvited(OfflinePlayer query) { return isInvited(query.getUniqueId()); }

	public boolean hasInviteExpired(UUID query) { return System.currentTimeMillis() - invites.get(query) > InviteTimeout; }
	public boolean hasInviteExpired(OfflinePlayer query) { return hasInviteExpired(query.getUniqueId()); }

	// - Territory - \\
	public List<KingdomChunk> getTerritory() { return territory; }
	public boolean isInTerritory(KingdomChunk chunk) { return territory.contains(chunk); }
	public boolean isInTerritory(Chunk chunk) { return territory.contains(KingdomChunk.from(chunk)); }
	public boolean isInTerritory(Location location) { return isInTerritory(location.getChunk()); }

	public @Nullable KingdomLocation getHome() { return home; }

	public Map<String, KingdomLocation> getWarps() { return warps; }
	@Nullable public KingdomLocation getWarp(String name) { return warps.getOrDefault(name, null); }
	public boolean isValidWarp(String name) { return warps.containsKey(name); }

	// - Money - \\
	public double getBalance() { return balance; }
	public boolean hasEnoughMoney(double amount) { return balance >= amount; }

	// - Misc (maps) - \\
	public Map<KingdomRank, List<KingdomPerms>> getRankPerms() { return rankPerms; }
	public List<KingdomPerms> getPermsByRank(@NotNull KingdomRank rank) { return rankPerms.get(rank); }

	public boolean doesRankHavePerm(KingdomRank rank, KingdomPerms perm) { return getPermsByRank(rank).contains(perm); }

	public boolean hasPermission(UUID query, KingdomPerms perm) { return getPermsByRank(getRank(query)).contains(perm) || query.equals(getLeaderId()); }
	public boolean hasPermission(OfflinePlayer query, KingdomPerms perm) { return hasPermission(query.getUniqueId(), perm) || query.getUniqueId().equals(getLeaderId()); }

	public Map<KingdomFlags, Boolean> getFlags() { return flags; }
	public boolean getFlag(KingdomFlags flag) { return flags.get(flag); }

	public Map<UUID, KingdomRelation> getRelations() { return relations; }
	public KingdomRelation getRelation(@NotNull Kingdom query) {
		KingdomRelation relation = relations.getOrDefault(query.getKingdomId(), KingdomRelation.NEUTRAL);
		if(relation == null) return KingdomRelation.NEUTRAL;
		return relation;
	}
	public KingdomRelation getRelation(Player query) { return Optional.ofNullable(fetchKingdom(query)).map(this::getRelation).orElse(KingdomRelation.NEUTRAL); }

	public List<Kingdom> getEnemies() { return relations.entrySet().stream().filter(e -> e.getValue() == KingdomRelation.ENEMY).map(e -> fetchKingdom(e.getKey())).toList(); }
	public List<Kingdom> getAllies() { return relations.entrySet().stream().filter(e -> e.getValue() == KingdomRelation.ALLY).map(e -> fetchKingdom(e.getKey())).toList(); }

	public boolean isAlly(@NotNull Kingdom query) { return getRelation(query) == KingdomRelation.ALLY; }
	public boolean isTruce(@NotNull Kingdom query) { return getRelation(query) == KingdomRelation.TRUCE; }
	public boolean isNeutral(@NotNull Kingdom query) { return getRelation(query) == KingdomRelation.NEUTRAL; }
	public boolean isEnemy(@NotNull Kingdom query) { return getRelation(query) == KingdomRelation.ENEMY; }
	public boolean isAtWar(@NotNull Kingdom query) { return getRelation(query) == KingdomRelation.AT_WAR; }

	public boolean isPersistent() { return isPersistent; }
	public double getPowerBoost() { return powerBoost; }

	// -- Setters -- \\
	// - Display - \\
	public void renameKingdom(String newName) throws KingdomException {
		if(newName.isBlank() || newName.isEmpty()) throw new KingdomException("The kingdom name cannot be empty");
		if(newName.length() > MAX_NAME_LENGTH) throw new KingdomException("The kingdom name cannot be longer than " + MAX_NAME_LENGTH + " characters");
		if(doesKingdomExist(newName)) throw new KingdomException("A kingdom with the name " + newName + " already exists");

		this.name = newName;
		kingdomIndex.put(kingdomId, newName);
	}

	public void setDescription(String description) throws KingdomException {
		if(description.length() > MAX_DESCRIPTION_LENGTH) throw new KingdomException("The kingdom description cannot be longer than " + MAX_DESCRIPTION_LENGTH + " characters");
		this.description = description;
	}

	public void setMotd(@Nullable String motd) throws KingdomException {
		if(motd != null && motd.length() > MAX_MOTD_LENGTH) throw new KingdomException("The kingdom MOTD cannot be longer than " + MAX_MOTD_LENGTH + " characters");
		this.motd = motd;
	}

	public void setTag(TextComponent tag) throws KingdomException {
		if(tag.content().length() > MAX_TAG_LENGTH) throw new KingdomException("The kingdom tag cannot be longer than " + MAX_TAG_LENGTH + " characters");

		Component finishedComp = Component.text("[").color(NamedTextColor.GRAY).append(tag).append(Component.text("]").color(NamedTextColor.GRAY));
		this.tag = LegacyComponentSerializer.legacyAmpersand().serialize(finishedComp);
	}

	// - Members - \\
	public void addMember(Player player) {
		members.put(player.getUniqueId(), KingdomRank.RECRUIT);
		player.getPersistentDataContainer().set(getPDCKingdomKey(), PersistentDataType.STRING, kingdomId.toString());
	}
	public void removeMember(Player player) {
		members.remove(player.getUniqueId());
		player.getPersistentDataContainer().remove(getPDCKingdomKey());
	}
	public void kickMember(OfflinePlayer player) {
		if(player.getUniqueId().equals(getLeaderId())) return;

		if(player.getPlayer() != null)
			player.getPlayer().getPersistentDataContainer().remove(getPDCKingdomKey());

		members.remove(player.getUniqueId());
	}

	public void setRank(OfflinePlayer player, KingdomRank rank, boolean force) {
		if(!force && rank == KingdomRank.LEADER) return;
		members.put(player.getUniqueId(), rank);
	}

	public void resignLeader(OfflinePlayer successor) {
		UUID prevLeader = getLeaderId();
		members.put(successor.getUniqueId(), KingdomRank.LEADER);
		members.put(prevLeader, KingdomRank.OFFICER);
	}

	public void invitePlayer(OfflinePlayer player) { invites.put(player.getUniqueId(), System.currentTimeMillis()); }
	public void revokeInvitation(OfflinePlayer player) { invites.remove(player.getUniqueId()); }

	// - Territory - \\
	public void claimTerritory(KingdomChunk chunk) {
		if(chunk.getChunk() == null) return;
		territory.add(chunk);
		chunk.getChunk().getPersistentDataContainer().set(getPDCKingdomKey(), PersistentDataType.STRING, kingdomId.toString());
	}

	public void claimTerritory(Chunk chunk) { claimTerritory(KingdomChunk.from(chunk)); }
	public void claimTerritory(Location location) { claimTerritory(location.getChunk()); }

	public void unclaimTerritory(KingdomChunk chunk) {
		if(chunk.getChunk() == null) return;

		territory.remove(chunk);
		chunk.getChunk().getPersistentDataContainer().remove(getPDCKingdomKey());
	}

	public void unclaimTerritory(Chunk chunk) { unclaimTerritory(KingdomChunk.from(chunk)); }
	public void unclaimTerritory(Location location) { unclaimTerritory(location.getChunk()); }
	public void unclaimAllTerritory() {
		territory.forEach(chunk -> Objects.requireNonNull(chunk.getChunk()).getPersistentDataContainer().remove(getPDCKingdomKey()));
		territory.clear();
	}

	public void setHome(@Nullable Location location) { home = (location == null ? null: new KingdomLocation(location)); }

	public void setWarp(String name, KingdomLocation location) { warps.put(name, location); }
	public void setWarp(String name, Location location) { setWarp(name, new KingdomLocation(location)); }
	public void removeWarp(String name) { warps.remove(name); }

	// - Money - \\
	public void depositMoney(double amount) { balance += amount; }
	public void withdrawMoney(double amount) { balance -= amount; }

	// - Misc (maps) - \\
	public void setRankPerms(KingdomRank rank, List<KingdomPerms> perms) { rankPerms.put(rank, perms); }
	public void addPermToRank(KingdomRank rank, KingdomPerms perm) { rankPerms.get(rank).add(perm); }
	public void removePermFromRank(KingdomRank rank, KingdomPerms perm) { rankPerms.get(rank).remove(perm); }

	public void setFlag(KingdomFlags flag, boolean value) { flags.put(flag, value); }

	public void setRelation(@NotNull Kingdom query, KingdomRelation relation) { relations.put(query.getKingdomId(), relation); }
	public void removeRelation(@NotNull Kingdom query) { relations.remove(query.getKingdomId()); }

	public void setPersistent(boolean isPersistent) { this.isPersistent = isPersistent; }
	public void setPowerBoost(double powerBoost) { this.powerBoost = powerBoost; }

	// -- Misc -- \\
	public double getPower() {
		return members.keySet().stream()
				.map(Bukkit::getOfflinePlayer)
				.map(KingdomPlayer::fetchPlayer)
				.filter(Objects::nonNull)
				.map(KingdomPlayer::getPower)
				.reduce(Double::sum)
				.orElse(0.0) * getPowerBoost();
	}

	public double getMaxPower() {
		return members.keySet().stream()
				.map(Bukkit::getOfflinePlayer)
				.map(KingdomPlayer::fetchPlayer)
				.filter(Objects::nonNull)
				.map(KingdomPlayer::getMaxPower)
				.reduce(Double::sum)
				.orElse(0.0) * getPowerBoost();
	}

	public void sendKingdomMessage(ComponentLike component) {
		getAllOnlineMembers().forEach(player -> {
			player.sendMessage(
					Component.text("[").color(NamedTextColor.GRAY)
							.append(Component.text(name).color(NamedTextColor.YELLOW))
							.append(Component.text("] ").color(NamedTextColor.GRAY))
							.append(Component.text("Kingdom Advisor").color(NamedTextColor.GOLD))
							.append(Component.text(" » ").color(NamedTextColor.GRAY))
							.append(Component.text()
									.color(NamedTextColor.WHITE)
									.append(component)
							)
			);
		});
	}

	public void sendKingdomMessage(Player from, ComponentLike component) {
		getAllOnlineMembers().forEach(player -> {
			player.sendMessage(
					Component.text("[").color(NamedTextColor.GRAY)
							.append(Component.text(name).color(NamedTextColor.YELLOW))
							.append(Component.text("] ").color(NamedTextColor.GRAY))
							.append(Component.text(from.getName()).color(NamedTextColor.GOLD))
							.append(Component.text(" » ").color(NamedTextColor.GRAY))
							.append(Component.text()
									.color(NamedTextColor.WHITE)
									.append(component)
							)
			);
		});

		sendSocialSpyMessage(this, from, component, false);
	}

	public void sendAllyMessage(ComponentLike component) {
		getAllOnlineMembers().forEach(player -> {
			player.sendMessage(
					Component.text("[").color(NamedTextColor.GRAY)
							.append(Component.text(name).color(NamedTextColor.YELLOW))
							.append(Component.text("] ").color(NamedTextColor.GRAY))
							.append(Component.text("Kingdom Advisor").color(NamedTextColor.GOLD))
							.append(Component.text(" » ").color(NamedTextColor.GRAY))
							.append(Component.text()
									.color(NamedTextColor.WHITE)
									.append(component)
							)
			);
		});

		getAllies().forEach(ally -> ally.getAllOnlineMembers().forEach(player -> {
			player.sendMessage(
					Component.text("[").color(NamedTextColor.GRAY)
							.append(Component.text(name).color(NamedTextColor.YELLOW))
							.append(Component.text("] ").color(NamedTextColor.GRAY))
							.append(Component.text(name + "'s Advisor").color(NamedTextColor.GOLD))
							.append(Component.text(" » ").color(NamedTextColor.GRAY))
							.append(Component.text()
									.color(NamedTextColor.WHITE)
									.append(component)
							)
			);
		}));
	}

	public void sendAllyMessage(Player from, ComponentLike component) {
		getAllOnlineMembers().forEach(player -> {
			player.sendMessage(
					Component.text("[Allies] ").color(NamedTextColor.AQUA)
							.append(Component.text("[").color(NamedTextColor.GRAY))
							.append(Component.text(name).color(NamedTextColor.YELLOW))
							.append(Component.text("] ").color(NamedTextColor.GRAY))
							.append(Component.text(from.getName()).color(NamedTextColor.GOLD))
							.append(Component.text(" » ").color(NamedTextColor.GRAY))
							.append(Component.text()
									.color(NamedTextColor.WHITE)
									.append(component)
							)
			);
		});

		getAllies().forEach(ally -> ally.getAllOnlineMembers().forEach(player -> {
			player.sendMessage(
					Component.text("[Allies] ").color(NamedTextColor.AQUA)
							.append(Component.text("[").color(NamedTextColor.GRAY))
							.append(Component.text(name).color(NamedTextColor.YELLOW))
							.append(Component.text("] ").color(NamedTextColor.GRAY))
							.append(Component.text(from.getName()).color(NamedTextColor.GOLD))
							.append(Component.text(" » ").color(NamedTextColor.GRAY))
							.append(Component.text()
									.color(NamedTextColor.WHITE)
									.append(component)
							)
			);
		}));

		sendSocialSpyMessage(this, from, component, true);
	}

	public void disband() {
		territory.forEach(chunk -> Objects.requireNonNull(chunk.getChunk()).getPersistentDataContainer().remove(getPDCKingdomKey()));
		territory.clear();

		members.keySet().forEach(uuid -> {
			OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
			if(player.getPlayer() != null)
				player.getPlayer().getPersistentDataContainer().remove(getPDCKingdomKey());
		});
		members.clear();

		invites.clear();
		home = null;
		warps.clear();

		delete();
	}

	public void save() { cache.setData(kingdomId, this); }

	public void delete() {
		cache.deleteData(kingdomId);
		getKingdomIndex().remove(kingdomId);
	}

	// -- Comparisons -- \\
	@Override public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Kingdom kingdom)) return false;

		return timeCreated == kingdom.timeCreated && kingdomId.equals(kingdom.kingdomId);
	}

	@Override public int hashCode() {
		int result = kingdomId.hashCode();
		result = 31 * result + Long.hashCode(timeCreated);
		return result;
	}
}