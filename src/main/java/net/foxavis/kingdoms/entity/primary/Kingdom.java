package net.foxavis.kingdoms.entity.primary;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.entity.Describable;
import net.foxavis.kingdoms.entity.KingdomFlag;
import net.foxavis.kingdoms.entity.KingdomRank;
import net.foxavis.kingdoms.entity.Territorial;
import net.foxavis.kingdoms.entity.relations.KingdomRelation;
import net.foxavis.kingdoms.entity.locations.KingdomChunk;
import net.foxavis.kingdoms.entity.locations.KingdomLocation;
import net.foxavis.kingdoms.entity.relations.PendingRelation;
import net.foxavis.kingdoms.entity.taxes.TaxRate;
import net.foxavis.kingdoms.util.CachedDataManager;
import net.foxavis.kingdoms.util.FileEncryption;
import net.foxavis.kingdoms.util.KingdomException;

import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Kingdom implements Describable, Territorial {

	// --- Static --- \\
	// -- Constants -- \\
	public static final int MAX_NAME_LENGTH = 32; // The maximum length of a Kingdom name
	public static final int MAX_DESCRIPTION_LENGTH = 48; // The maximum length of a Kingdom description
	public static final int MAX_MOTD_LENGTH = 512; // The maximum length of a Kingdom message of the day
	public static final int MAX_TAG_LENGTH = 5; // The maximum length of a Kingdom tag

	public static final long INVITATION_TIMEOUT = TimeUnit.DAYS.toMillis(3); // The time in milliseconds before an invitation expires

	public static final double TAX_KINGDOM = 50.0D; // The flat tax for a Kingdom
	public static final double TAX_SETTLEMENT = 10.0D; // The flat tax for each Settlement
	public static final double TAX_NON_SETTLEMENT_CHUNK = 1.5D; // The flat tax for each non-Settlement chunk
	public static final double TAX_SETTLEMENT_CHUNK = 0.6D; // The flat tax for each Settlement chunk

	private static final CachedDataManager<UUID, Kingdom> cache = new CachedDataManager<>(10, TimeUnit.MINUTES) {
		@Override protected @Nullable Kingdom loadFromSource(@NotNull UUID key) {
			File file = new File(getKingdomFolder(), key + ".kingdom");
			if (!file.exists()) return null;

			String contents = CachedDataManager.readContents(file);
			if (contents == null) return null;

			try {
				String decrypted = FileEncryption.decrypt("AES", contents, FoxavisKingdoms.getSecretKey());
				return FoxavisKingdoms.getGSON().fromJson(decrypted, Kingdom.class);
			} catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException |
					 IllegalBlockSizeException | BadPaddingException e) {
				FoxavisKingdoms.getLoggerInstance().log(Level.SEVERE, "Failed to decrypt Kingdom data for " + key + ".", e);
				throw new RuntimeException("Failed to decrypt Kingdom data for " + key + ".", e);
			}
		}

		@Override protected void saveToSource(@NotNull UUID key, @NotNull Kingdom value) {
			File file = new File(getKingdomFolder(), key + ".kingdom");

			try {
				String json = FoxavisKingdoms.getGSON().toJson(value);
				String encrypted = FileEncryption.encrypt("AES", json, FoxavisKingdoms.getSecretKey());
				CachedDataManager.writeContents(file, encrypted);
			} catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException |
					 IOException | InvalidKeyException e) {
				FoxavisKingdoms.getLoggerInstance().log(Level.SEVERE, "Failed to encrypt and save Kingdom data for " + key + ".", e);
				throw new RuntimeException("Failed to encrypt and save Kingdom data for " + key + ".", e);
			}
		}

		@Override protected void deleteFromSource(@NotNull UUID key) {
			File file = new File(getKingdomFolder(), key + ".kingdom");
			if (!file.exists()) return;
			if (!file.delete()) {
				FoxavisKingdoms.getLoggerInstance().log(Level.SEVERE, "Failed to delete Kingdom data for " + key + ".");
				throw new RuntimeException("Failed to delete Kingdom data for " + key + ".");
			}
		}
	};
	private static Map<UUID, String> kingdomIndex = null;

	// -- Methods -- \\
	/**
	 * Returns the key used to identify Kingdoms in the plugin.
	 * <p>This is used in {@link org.bukkit.persistence.PersistentDataContainer} to store Kingdom data for chunks and players.</p>
	 * @return The key used to identify Kingdoms in the plugin.
	 */
	public static NamespacedKey getKingdomKey() { return new NamespacedKey(FoxavisKingdoms.getInstance(), "kingdom"); }

	/**
	 * Gets the cache used to store Kingdom data.
	 * @return The cache used to store Kingdom data.
	 */
	public static CachedDataManager<UUID, Kingdom> getCache() { return cache; }

	/**
	 * Returns a map of all Kingdom IDs and their associated names.
	 * @return A map of all Kingdom IDs and their associated names.
	 */
	public static Map<UUID, String> getKingdomIndex() {
		if(kingdomIndex == null) {
			try {
				File file = new File(getKingdomFolder(), "index.json");
				if(file.createNewFile()) {
					kingdomIndex = new HashMap<>();
					CachedDataManager.writeContents(file, FoxavisKingdoms.getGSON().toJson(kingdomIndex));
				} else {
					String contents = CachedDataManager.readContents(file);
					if(contents == null) throw new FileSystemException("Failed to read Kingdom index file.");
					kingdomIndex = FoxavisKingdoms.getGSON().fromJson(contents, new TypeToken<Map<UUID, String>>(){}.getType());
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return kingdomIndex;
	}

	/**
	 * Saves the Kingdom index to file. This should be done on shutdown.
	 */
	public static void saveKingdomIndex() {
		File file = new File(getKingdomFolder(), "index.json");

		try {
			if(kingdomIndex == null) getKingdomIndex();
			CachedDataManager.writeContents(file, FoxavisKingdoms.getGSON().toJson(kingdomIndex));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Fetches a Kingdom using the cache from the Kingdom ID.
	 * @param kingdomId The ID of the Kingdom to fetch.
	 * @return The Kingdom with the specified ID, or null if no Kingdom exists with that ID.
	 */
	@Nullable public static Kingdom fetchKingdom(@Nullable UUID kingdomId) {
		if(kingdomId == null) return null;
		return cache.fetchData(kingdomId);
	}

	/**
	 * Using the Kingdom Index, fetches a Kingdom using the Kingdom name (case-insensitive).
	 * @param kingdomName The case-insensitive name of the Kingdom to fetch.
	 * @return The Kingdom with the specified name, or null if no Kingdom exists with that name.
	 */
	@Nullable public static Kingdom fetchKingdom(@NotNull String kingdomName) {
		return fetchKingdom(getKingdomIndex().entrySet().stream()
				.filter(entry -> entry.getValue().equalsIgnoreCase(kingdomName))
				.map(Map.Entry::getKey)
				.findFirst()
				.orElse(null)
		);
	}

	/**
	 * Using the Player's PersistentDataContainer, fetches the Kingdom the Player is a member of.
	 * @param player The Player to fetch the Kingdom for.
	 * @return The Kingdom the Player is a member of, or null if the Player is not a member of any Kingdom.
	 */
	@Nullable public static Kingdom fetchKingdom(@NotNull Player player) {
		String stringifiedId = player.getPersistentDataContainer().get(getKingdomKey(), PersistentDataType.STRING);
		if(stringifiedId == null) return null;
		return fetchKingdom(UUID.fromString(stringifiedId));
	}

	/**
	 * Using the Kingdom Index, attempts to see if there is a kingdom with the specified name.
	 * @param kingdomName The case-insensitive name of the Kingdom to check for.
	 * @return True if a Kingdom with the specified name exists, false otherwise.
	 */
	public static boolean doesKingdomExist(String kingdomName) {
		return getKingdomIndex().values().stream().anyMatch(name -> name.equalsIgnoreCase(kingdomName));
	}

	/**
	 * Using the Kingdom Index, attempts to see if there is a kingdom with the specified ID.
	 * @param kingdomId The ID of the Kingdom to check for.
	 * @return True if a Kingdom with the specified ID exists, false otherwise.
	 */
	public static boolean doesKingdomExist(UUID kingdomId) {
		return getKingdomIndex().containsKey(kingdomId);
	}

	/**
	 * Returns the folder where Kingdom data is stored.
	 * @return The folder where Kingdom data is stored.
	 */
	private static File getKingdomFolder() {
		File pluginDataFolder = FoxavisKingdoms.getInstance().getDataFolder();
		if(!pluginDataFolder.exists())
			if(!pluginDataFolder.mkdirs())
				throw new RuntimeException("Failed to create plugin data folder.");
		File kingdomFolder = new File(pluginDataFolder, "kingdoms");

		if(!kingdomFolder.exists()) {
			if(!kingdomFolder.mkdirs())
				throw new RuntimeException("Failed to create kingdom data folder.");
		}

		return kingdomFolder;
	}

	/**
	 * A utility method to translate a list of UUIDs (such as members) to a list of OfflinePlayers instead.
	 * @param uuids The list of UUIDs to translate.
	 * @return A list of OfflinePlayers representing the UUIDs.
	 */
	public static @NotNull List<OfflinePlayer> translateToOfflinePlayers(@NotNull Collection<UUID> uuids) {
		return uuids.stream().map(Bukkit::getOfflinePlayer).toList();
	}

	/**
	 * A utility method to translate a list of UUIDs (such as members) to a list of online Players instead.
	 * @param uuids The list of UUIDs to translate.
	 * @return A list of Players representing the UUIDs.
	 */
	public static @NotNull List<Player> translateToOnlinePlayers(@NotNull Collection<UUID> uuids) {
		return uuids.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
	}

	/**
	 * A utility method to filter a list of OfflinePlayers to only include those that are online.
	 * @param offlinePlayers The list of OfflinePlayers to filter.
	 * @return A list of Players representing the online OfflinePlayers.
	 */
	public static @NotNull List<Player> filterOnlineOnly(@NotNull Collection<OfflinePlayer> offlinePlayers) {
		return offlinePlayers.stream().map(OfflinePlayer::getPlayer).filter(Objects::nonNull).toList();
	}

	//<editor-fold desc="Fields>
	// --- Fields --- \\
	// -- Identifiers -- \\
	@NotNull private UUID kingdomId; // Primary Key: The unique identifier for this Kingdom

	// -- Display -- \\
	@NotNull private String name; // The name of the Kingdom
	@Nullable private String description; // A short description of the Kingdom
	@Nullable private String motd; // The message of the day for the Kingdom
	@Nullable private String rawTag; // The raw tag for the Kingdom, translated from a TextComponent

	private long timeCreated; // The time this Kingdom was created

	// -- Members -- \\
	@NotNull private Map<Integer, KingdomRank> ranks; // The ranks within the Kingdom
	@NotNull private UUID foundingMember; // The founding member of the Kingdom

	@NotNull private Map<UUID, Integer> members; // The members of the Kingdom (member UUID, rank ID)
	@NotNull private Map<UUID, Long> invitations; // The invitations to the Kingdom (member UUID, time invited)

	// -- Territory -- \\
	@NotNull private HashSet<KingdomChunk> territory; // The territory of the Kingdom
	@NotNull private HashSet<Settlement> settlements; // The settlements within the Kingdom

	@Nullable private KingdomLocation home; // The home location of the Kingdom
	@NotNull private Map<String, KingdomLocation> warps; // The warps within the Kingdom

	@NotNull private Map<KingdomFlag, Boolean> flags; // The flags of the Kingdom

	// -- Money -- \\
	private double balance; // The balance of the Kingdom
	@Nullable private TaxRate taxRate; // The tax rate of the Kingdom

	// -- Relations -- \\
	@NotNull private Map<UUID, KingdomRelation> relations; // The relations of the Kingdom
	@NotNull private Map<UUID, PendingRelation> pendingRelations; // The pending relations of the Kingdom (target kingdom ID, pending relation)

	// -- Boolean Flags -- \\ (moved to flags KEKW)

	// -- Misc -- \\
	private double powerBoost; // The power boost of the Kingdom
	//</editor-fold>

	// --- Constructors --- \\
	public Kingdom(@NotNull Player founder, @NotNull String name) throws KingdomException {
		if(name.isBlank() || name.isEmpty()) throw new KingdomException("The name of the Kingdom cannot be blank or empty.");
		if(name.length() > MAX_NAME_LENGTH) throw new KingdomException("The name of the Kingdom cannot be longer than " + MAX_NAME_LENGTH + " characters.");
		if(doesKingdomExist(name)) throw new KingdomException("A Kingdom with the name \"" + name + "\" already exists.");

		do this.kingdomId = UUID.randomUUID(); while (doesKingdomExist(kingdomId)); // Generate a unique identifier for the Kingdom

		this.name = name;
		this.description = null;
		this.motd = null;
		// set the tag to the first 5 characters of the name with a gray color

		this.timeCreated = System.currentTimeMillis();

		this.ranks = new HashMap<>();
		this.foundingMember = founder.getUniqueId();

		this.members = new HashMap<>();
		this.invitations = new HashMap<>();

		this.territory = new HashSet<>();
		this.settlements = new HashSet<>();

		this.home = null;
		this.warps = new HashMap<>();

		this.flags = new HashMap<>();

		this.balance = 0;
		this.taxRate = null;

		this.relations = new HashMap<>();
		this.pendingRelations = new HashMap<>();

		this.powerBoost = 0;

		cache.storeData(this.kingdomId, this);
		getKingdomIndex().put(this.kingdomId, this.name);
	}

	// --- Methods --- \\
	// -- Getters -- \\
	// - Identifiers - \\
	/**
	 * Returns the unique identifier for this Kingdom. This is the primary key for the Kingdom.
	 * @return The unique identifier for this Kingdom.
	 */
	public @NotNull UUID getKingdomId() { return kingdomId; }

	// - Display - \\
	/**
	 * Returns the name of the Kingdom. This is what most players will refer to the Kingdom as.
	 * @return The name of the Kingdom.
	 */
	public @NotNull String getName() { return name; }

	/**
	 * Returns a short description of the Kingdom. This is a brief overview of the Kingdom.
	 * @return A short description of the Kingdom.
	 */
	public @Nullable String getDescription() { return description; }

	/**
	 * Returns the message of the day for the Kingdom. This is a message that is displayed to all members of the Kingdom when they log in.
	 * @return The message of the day for the Kingdom.
	 */
	public @Nullable String getMotd() { return motd; }

	/**
	 * Returns the raw tag for the Kingdom.
	 * <p>This should not be used, instead please use {@link #getTag()} to display normally.</p>
	 * @return The raw tag of the kingdom, or null if one is not set.
	 */
	public @Nullable String getRawTag() { return rawTag; }

	/**
	 * Returns the display tag for the Kingdom. This should be the first thing displayed in chat.
	 * <p>If a kingdom does not have a rawTag set (if null), then a pregenerated one will be used using the first 5 letters of its name.</p>
	 * @return The display tag for the Kingdom.
	 */
	public @NotNull TextComponent getTag() {
		if(rawTag == null) { // Check if a kingdom has a tag or not.
			// If not, create it for them, using the first 5 letters of their name.
			return Component.text()
					.append(Component.text("[").color(NamedTextColor.GRAY))
					.append(Component.text(name.substring(0, Math.min(name.length(), MAX_TAG_LENGTH))).color(NamedTextColor.GRAY))
					.append(Component.text("]").color(NamedTextColor.GRAY))
					.hoverEvent(HoverEvent.showText(Component.text()
							.append(Component.text("This player is a part of the ").color(NamedTextColor.WHITE))
							.append(Component.text(getName()).color(NamedTextColor.GOLD))
							.append(Component.text( " kingdom.").color(NamedTextColor.WHITE))
					)).build();
		} else {
			// Otherwise, use the tag that was set.
			return Component.text()
					.append(Component.text("[").color(NamedTextColor.GRAY))
					.append(LegacyComponentSerializer.legacyAmpersand().deserialize(rawTag).colorIfAbsent(NamedTextColor.GRAY))
					.append(Component.text("]").color(NamedTextColor.GRAY))
					.hoverEvent(HoverEvent.showText(Component.text()
							.append(Component.text("This player is a part of the ").color(NamedTextColor.WHITE))
							.append(Component.text(getName()).color(NamedTextColor.GOLD))
							.append(Component.text( " kingdom.").color(NamedTextColor.WHITE))
					)).build();
		}
	}

	/**
	 * Returns the time this Kingdom was created, in milliseconds since the Unix epoch.
	 * @return The time this Kingdom was created.
	 */
	public long getTimeCreated() { return timeCreated; }

	// - Members - \\
	// Ranks Map
	/**
	 * Returns the raw map of ranks within the Kingdom.
	 * @return A map, where the key is the rank ID and the value is the KingdomRank object.
	 */
	public @NotNull Map<Integer, KingdomRank> getRanksMap() { return ranks; }

	/**
	 * Returns a list of ranks within the Kingdom.
	 * @return A list of ranks within the Kingdom.
	 */
	public @NotNull List<KingdomRank> getRanks() { return new ArrayList<>(ranks.values()); }

	/**
	 * Gets a KingdomRank object by its ID within the Kingdom.
	 * @param rankId The ID of the rank to fetch.
	 * @return The KingdomRank object with the specified ID, or null if no rank exists with that ID.
	 */
	public @Nullable KingdomRank getRankById(int rankId) { return ranks.get(rankId); }

	// Founding Member
	/**
	 * Returns the founding member's ID of the Kingdom. This is the player who created the Kingdom.
	 * @return The founding member of the Kingdom.
	 */
	public @NotNull UUID getFoundingMemberId() { return foundingMember; }

	/**
	 * Returns the founding member of the Kingdom. This is the player who created the Kingdom.
	 * @return An OfflinePlayer object representing the founding member of the Kingdom.
	 */
	public @NotNull OfflinePlayer getFoundingMember() { return Bukkit.getOfflinePlayer(foundingMember); }

	// Members Map
	/**
	 * Returns the raw map of members within the Kingdom.
	 * @return A map, where the key is the member UUID and the value is the rank ID.
	 */
	public @NotNull Map<UUID, Integer> getMembersMap() { return members; }

	/**
	 * Gathers a list of all member IDs within the Kingdom.
	 * @return A list of all member IDs within the Kingdom.
	 */
	public @NotNull List<UUID> getMembers() { return new ArrayList<>(members.keySet()); }

	/**
	 * Determines if a player is a member of the Kingdom.
	 * @param memberId The ID of the player to check.
	 * @return True if the player is a member of the Kingdom, false otherwise.
	 */
	public boolean isMember(UUID memberId) { return members.containsKey(memberId); }

	/**
	 * Determines if a player is a member of the Kingdom.
	 * @param player The player to check.
	 * @return True if the player is a member of the Kingdom, false otherwise.
	 */
	public boolean isMember(OfflinePlayer player) { return isMember(player.getUniqueId()); }

	/**
	 * Determines if a player is a member of the Kingdom.
	 * @param rankId The ID of the rank to check for.
	 * @return A list of all member IDs within the Kingdom that have the specified rank.
	 */
	public @NotNull List<UUID> getMembersByRank(int rankId) { return members.entrySet().stream().filter(entry -> entry.getValue() == rankId).map(Map.Entry::getKey).toList(); }

	/**
	 * Determines if a player is a member of the Kingdom. This is a convenience method that uses a KingdomRank object instead of an ID.
	 * @param rank The rank to check for.
	 * @return A list of all member IDs within the Kingdom that have the specified rank.
	 * @see #getMembersByRank(int)
	 */
	public @NotNull List<UUID> getMembersByRank(KingdomRank rank) { return getMembersByRank(rank.getRankId()); }

	/**
	 * Determines if a player of the kingdom has a specific rank.
	 * @param memberId The ID of the player to check.
	 * @param rankId The ID of the rank to check for.
	 * @return True if the player has the specified rank, false otherwise.
	 */
	public boolean hasRank(UUID memberId, int rankId) { if(!members.containsKey(memberId)) return false; else return members.get(memberId) == rankId; }

	/**
	 * Determines if a player of the kingdom has a specific rank.
	 * @param player The player to check.
	 * @param rankId The ID of the rank to check for.
	 * @return True if the player has the specified rank, false otherwise.
	 */
	public boolean hasRank(OfflinePlayer player, int rankId) { return hasRank(player.getUniqueId(), rankId); }

	/**
	 * Gets the rank of a member within the Kingdom.
	 * @param member The ID of the player to check.
	 * @return The rank of the player, or null if the player is not a member of the Kingdom.
	 */
	public @Nullable KingdomRank getRank(UUID member) { if(!members.containsKey(member)) return null; else return ranks.get(members.get(member)); }

	/**
	 * Gets the rank of a player within the Kingdom.
	 * @param member The player to check.
	 * @return The rank of the player, or null if the player is not a member of the Kingdom.
	 */
	public @Nullable KingdomRank getRank(OfflinePlayer member) { return getRank(member.getUniqueId()); }

	// Invitations Map
	/**
	 * Returns the raw map of invitations to the Kingdom.
	 * @return A map, where the key is the member UUID and the value is the time they were invited.
	 */
	public Map<UUID, Long> getInvitationsMap() { return invitations; }

	/**
	 * Gathers a list of all invited member IDs to the Kingdom.
	 * @param memberId The ID of the player to check.
	 * @return True if the player is invited to the Kingdom, false otherwise.
	 */
	public boolean isInvited(UUID memberId) { return invitations.containsKey(memberId); }

	/**
	 * Gathers a list of all invited member IDs to the Kingdom.
	 * @param player The player to check.
	 * @return True if the player is invited to the Kingdom, false otherwise.
	 */
	public boolean isInvited(OfflinePlayer player) { return isInvited(player.getUniqueId()); }

	/**
	 * Determines if an invitation to the Kingdom has expired.
	 * @param memberId The ID of the player to check.
	 * @return True if the invitation has expired, false otherwise.
	 */
	public boolean hasInviteExpired(UUID memberId) { if(!isInvited(memberId)) return false; else return System.currentTimeMillis() - invitations.get(memberId) > INVITATION_TIMEOUT; }

	/**
	 * Determines if an invitation to the Kingdom has expired.
	 * @param player The player to check.
	 * @return True if the invitation has expired, false otherwise.
	 */
	public boolean hasInviteExpired(OfflinePlayer player) { return hasInviteExpired(player.getUniqueId()); }

	// - Territory - \\
	// Territory Set
	/**
	 * Returns a list of all chunks currently claimed by this territory.
	 * @return A list of all claimed chunks associated with this territory.
	 */
	@Override public @NotNull HashSet<KingdomChunk> getTerritory() { return territory; }

	/**
	 * Checks if the specified {@link KingdomChunk} is claimed by this territory.
	 * @param chunk The chunk to check.
	 * @return True if the chunk is claimed, false otherwise.
	 */
	@Override public boolean isInTerritory(KingdomChunk chunk) { return territory.contains(chunk); }

	/**
	 * Checks if the specified {@link Chunk} is claimed by this territory.
	 * @param chunk The chunk to check.
	 * @return True if the chunk is claimed, false otherwise.
	 */
	@Override public boolean isInTerritory(Chunk chunk) { return territory.contains(new KingdomChunk(chunk)); }

	/**
	 * Checks if the chunk corresponding to the specified {@link Location} is claimed by this territory.
	 * @param location The location to check.
	 * @return True if the chunk containing the location is claimed, false otherwise.
	 */
	@Override public boolean isInTerritory(Location location) { return territory.contains(new KingdomChunk(location)); }

	// Settlements Set
	/**
	 * Returns a list of all settlements within the Kingdom.
	 * @return A list of all settlements within the Kingdom.
	 */
	public @NotNull HashSet<Settlement> getSettlements() { return settlements; }

	/**
	 * Checks if the specified {@link Settlement} is within the Kingdom.
	 * @param name The name of the settlement to check.
	 * @return The settlement within the Kingdom, or null if no settlement exists with that name.
	 */
	public @Nullable Settlement getSettlement(String name) { return settlements.stream().filter(settlement -> settlement.getName().equalsIgnoreCase(name)).findFirst().orElse(null); }

	/**
	 * Gets the settlement that contains the specified {@link KingdomChunk}.
	 * @param chunk The chunk of the settlement to check.
	 * @return The settlement within the Kingdom, or null if no settlement exists in that chunk.
	 */
	public @Nullable Settlement getSettlement(KingdomChunk chunk) { return settlements.stream().filter(settlement -> settlement.isInTerritory(chunk)).findFirst().orElse(null); }

	/**
	 * Gets the settlement that contains the specified {@link Chunk}.
	 * @param chunk The chunk of the settlement to check.
	 * @return The settlement within the Kingdom, or null if no settlement exists in that chunk.
	 */
	public @Nullable Settlement getSettlement(Chunk chunk) { return settlements.stream().filter(settlement -> settlement.isInTerritory(chunk)).findFirst().orElse(null); }

	/**
	 * Gets the settlement that contains the chunk corresponding to the specified {@link Location}.
	 * @param location The location of the settlement to check.
	 * @return The settlement within the Kingdom, or null if no settlement exists in that chunk.
	 */
	public @Nullable Settlement getSettlement(Location location) { return getSettlement(new KingdomChunk(location)); }

	// Home Location
	/**
	 * Returns the home location of the Kingdom.
	 * @return The home location of the Kingdom, or null if no home location is set.
	 */
	public @Nullable KingdomLocation getHome() { return home; }

	/**
	 * Sets the home location of the Kingdom.
	 * @return The home location of the Kingdom, or null if no home location is set.
	 */
	public boolean hasHome() { return home != null; }

	// Warps Map
	/**
	 * Returns the raw map of warps within the Kingdom.
	 * @return A map, where the key is the warp name and the value is the KingdomLocation object.
	 */
	public @NotNull Map<String, KingdomLocation> getWarpsMap() { return warps; }

	/**
	 * Gets the warp location with the specified name.
	 * @param name The name of the warp to fetch.
	 * @return The warp location with the specified name, or null if no warp exists with that name.
	 */
	@Nullable public KingdomLocation getWarp(String name) { return warps.get(name); }

	/**
	 * Determines if a warp with the specified name exists.
	 * @param name The name of the warp to check for.
	 * @return True if a warp with the specified name exists, false otherwise.
	 */
	public boolean isValidWarp(String name) { return warps.containsKey(name); }

	// Flags Map
	/**
	 * Returns the raw map of flags within the Kingdom.
	 * @return A map, where the key is the flag and the value is the boolean value.
	 */
	public @NotNull Map<KingdomFlag, Boolean> getFlagsMap() { return flags; }

	/**
	 * Gets the state of a flag within the Kingdom by the flag itself.
	 * @param flag The flag to check for.
	 * @return The state of the flag, or null if the flag does not exist.
	 */
	public boolean getFlagStateRaw(KingdomFlag flag) { return flags.get(flag); }

	/**
	 * Gets the state of a flag within the Kingdom by the flag itself.
	 * @param flag The flag to check for.
	 * @return The state of the flag, or the default value if the flag does not exist.
	 */
	public boolean getFlagState(KingdomFlag flag) { return flags.getOrDefault(flag, flag.defaultValue()); }

	// - Money - \\
	/**
	 * Returns the balance of the Kingdom. This is the amount of money the Kingdom has.
	 * @return The balance of the Kingdom.
	 */
	public double getBalance() { return balance; }

	/**
	 * Returns the tax rate of the Kingdom. This is the rate at which taxes are collected from the Kingdom.
	 * @return The tax rate of the Kingdom, or null if no tax rate is set.
	 * @see TaxRate
	 * @see net.foxavis.kingdoms.entity.taxes.FixedTaxRate
	 * @see net.foxavis.kingdoms.entity.taxes.PercentTaxRate
	 */
	public @Nullable TaxRate getTaxRate() { return taxRate; }

	// - Relations - \\
	// Relations Map
	/**
	 * Returns the raw map of relations within the Kingdom.
	 * @return A map, where the key is the target Kingdom ID and the value is the KingdomRelation object.
	 */
	public @NotNull Map<UUID, KingdomRelation> getRelationsMap() { return relations; }

	/**
	 * Gets the relation with the specified Kingdom.
	 * @param kingdom The Kingdom to check for.
	 * @return The relation with the specified Kingdom, or null if no relation exists.
	 */
	public @NotNull KingdomRelation getRelation(@NotNull Kingdom kingdom) { return relations.getOrDefault(kingdom.getKingdomId(), KingdomRelation.NEUTRAL); }

	/**
	 * Gets the relation with the specified player.
	 * @param player The player to check for.
	 * @return The relation with the specified player, or null if no relation exists.
	 */
	public @NotNull KingdomRelation getRelation(@NotNull Player player) { Kingdom kingdom = fetchKingdom(player); if(kingdom == null) return KingdomRelation.NEUTRAL; else return getRelation(kingdom); }

	/**
	 * Checks if the Kingdom is an ally with the specified Kingdom.
	 * @param kingdom The Kingdom to check for.
	 * @return True if the Kingdom is an ally with the specified Kingdom, false otherwise.
	 */
	public boolean isAlly(@NotNull Kingdom kingdom) { return getRelation(kingdom) == KingdomRelation.ALLY; }

	/**
	 * Checks if the Kingdom is truced with the specified Kingdom.
	 * @param kingdom The Kingdom to check for.
	 * @return True if the Kingdom is truced with the specified Kingdom, false otherwise.
	 */
	public boolean isTruce(@NotNull Kingdom kingdom) { return getRelation(kingdom) == KingdomRelation.TRUCE; }

	/**
	 * Checks if the Kingdom is neutral with the specified Kingdom.
	 * @param kingdom The Kingdom to check for.
	 * @return True if the Kingdom is neutral with the specified Kingdom, false otherwise.
	 */
	public boolean isNeutral(@NotNull Kingdom kingdom) { return getRelation(kingdom) == KingdomRelation.NEUTRAL; }

	/**
	 * Checks if the Kingdom is an enemy with the specified Kingdom.
	 * @param kingdom The Kingdom to check for.
	 * @return True if the Kingdom is an enemy with the specified Kingdom, false otherwise.
	 */
	public boolean isEnemy(@NotNull Kingdom kingdom) { return getRelation(kingdom) == KingdomRelation.ENEMY; }

	/**
	 * Checks if the Kingdom is at war with the specified Kingdom.
	 * @param kingdom The Kingdom to check for.
	 * @return True if the Kingdom is at war with the specified Kingdom, false otherwise.
	 */
	public boolean isAtWar(@NotNull Kingdom kingdom) { return getRelation(kingdom) == KingdomRelation.AT_WAR; }

	/**
	 * Gets a map of all kingdoms that this kingdom is allied with.
	 * @return A set of all kingdoms that this kingdom is allied with.
	 */
	public @NotNull Set<Kingdom> getAllies() { return relations.entrySet().stream().filter(entry -> entry.getValue() == KingdomRelation.ALLY).map(entry -> fetchKingdom(entry.getKey())).filter(Objects::nonNull).collect(Collectors.toSet()); }

	/**
	 * Gets a map of all kingdoms that this kingdom is truced with.
	 * @return A set of all kingdoms that this kingdom is truced with.
	 */
	public @NotNull Set<Kingdom> getTruces() { return relations.entrySet().stream().filter(entry -> entry.getValue() == KingdomRelation.TRUCE).map(entry -> fetchKingdom(entry.getKey())).filter(Objects::nonNull).collect(Collectors.toSet()); }

	/**
	 * Gets a map of all kingdoms that this kingdom is enemies with.
	 * @return A set of all kingdoms that this kingdom is enemies with.
	 */
	public @NotNull Set<Kingdom> getEnemies() { return relations.entrySet().stream().filter(entry -> entry.getValue() == KingdomRelation.ENEMY).map(entry -> fetchKingdom(entry.getKey())).filter(Objects::nonNull).collect(Collectors.toSet()); }

	/**
	 * Gets a map of all kingdoms that this kingdom is at war with.
	 * @return A set of all kingdoms that this kingdom is at war with.
	 */
	public @NotNull Set<Kingdom> getWars() { return relations.entrySet().stream().filter(entry -> entry.getValue() == KingdomRelation.AT_WAR).map(entry -> fetchKingdom(entry.getKey())).filter(Objects::nonNull).collect(Collectors.toSet()); }

	// Pending Relations Map
	/**
	 * Returns the raw map of pending relations within the Kingdom.
	 * @return A map, where the key is the target Kingdom ID and the value is the PendingRelation object.
	 */
	public @NotNull Map<UUID, PendingRelation> getPendingRelationsMap() { return pendingRelations; }

	/**
	 * Gets the pending relation with the specified Kingdom.
	 * @param kingdom The Kingdom to check for.
	 * @return The pending relation with the specified Kingdom, or null if no pending relation exists.
	 */
	public @Nullable PendingRelation getPendingRelation(@NotNull Kingdom kingdom) { return pendingRelations.get(kingdom.getKingdomId()); }

	/**
	 * Gets the pending relation with the specified player.
	 * @param kingdomId The ID of the Kingdom to check for.
	 * @return The pending relation with the specified Kingdom, or null if no pending relation exists.
	 */
	public @Nullable PendingRelation getPendingRelation(@NotNull UUID kingdomId) { return pendingRelations.get(kingdomId); }

	/**
	 * Checks if the Kingdom has a pending relation with the specified Kingdom.
	 * @param kingdom The Kingdom to check for.
	 * @return True if the Kingdom has a pending relation with the specified Kingdom, false otherwise.
	 */
	public boolean hasPendingRelation(@NotNull Kingdom kingdom) { return pendingRelations.containsKey(kingdom.getKingdomId()); }

	/**
	 * Checks if the Kingdom has a pending relation with the specified Kingdom.
	 * @param kingdomId The ID of the Kingdom to check for.
	 * @return True if the Kingdom has a pending relation with the specified Kingdom, false otherwise.
	 */
	public boolean hasPendingRelation(@NotNull UUID kingdomId) { return pendingRelations.containsKey(kingdomId); }

	// - Boolean Flags - \\
	// none lmao

	// - Misc - \\
	/**
	 * Returns the power boost of the Kingdom. This is a multiplier that affects the power of the Kingdom.
	 * @return The power boost of the Kingdom.
	 */
	public double getPowerBoost() { return powerBoost; }

	// -- Setters -- \\
	// - Identifiers - \\
	// none lmao

	// - Display - \\
	/**
	 * Sets the name of the Kingdom.
	 * @deprecated Use {@link #renameKingdom(String)} instead.
	 * @param newName the new name to set
	 */
	@Override public void setName(@NotNull String newName) {
		this.name = newName;
		getKingdomIndex().put(this.kingdomId, this.name);
	}

	/**
	 * Renames the Kingdom to the specified name.
	 * @param newName The new name to set.
	 * @throws KingdomException if the name is blank, empty, longer than {@link #MAX_NAME_LENGTH}, or if a Kingdom with the name already exists.
	 */
	public void renameKingdom(@NotNull String newName) throws KingdomException {
		if(name.isBlank() || name.isEmpty()) throw new KingdomException("The name of the Kingdom cannot be blank or empty.");
		if(name.length() > MAX_NAME_LENGTH) throw new KingdomException("The name of the Kingdom cannot be longer than " + MAX_NAME_LENGTH + " characters.");
		if(doesKingdomExist(name)) throw new KingdomException("A Kingdom with the name \"" + name + "\" already exists.");

		this.name = newName;
		getKingdomIndex().put(this.kingdomId, this.name);
	}

	@Override public void setDescription(@Nullable String description) { this.description = description; }

	/**
	 * Sets the message of the day for the Kingdom. This is a message that is displayed to all members of the Kingdom when they log in.
	 * @param motd The message of the day to set.
	 */
	public void setMotd(@Nullable String motd) { this.motd = motd; }

	/**
	 * Sets the tag for the Kingdom.
	 * @param tag The tag to set.
	 */
	public void setTag(@Nullable TextComponent tag) throws KingdomException {
		if(tag == null) { this.rawTag = null; return; }
		if(tag.content().length() > MAX_TAG_LENGTH) throw new KingdomException("A tag in a kingdom cannot be longer than " + MAX_TAG_LENGTH);

		this.rawTag = LegacyComponentSerializer.legacyAmpersand().serialize(tag);
	}

	// - Members - \\
	// Ranks Map
	/**
	 * Creates a new rank at the highest rank possible (which is the lowest rank)
	 * @param name The name of the rank to create
	 * @return The rank that was created
	 */
	public KingdomRank createRank(@NotNull String name) {
		int rankId = ranks.isEmpty() ? 0 : ranks.keySet().stream().max(Integer::compareTo).orElseThrow() + 1;
		KingdomRank rank = new KingdomRank(rankId, name);
		ranks.put(rankId, rank);
		return rank;
	}

	/**
	 * Places a rank at a specific rank ID. Useful for moving ranks around.
	 */
	public void putRank(@NotNull KingdomRank rank) {
		ranks.put(rank.getRankId(), rank);
	}

	public void deleteRank(int rankId) {
		ranks.remove(rankId);
		members.entrySet().stream()
				.filter(entry -> entry.getValue() == rankId)
				.forEach(entry -> members.put(entry.getKey(), 0));
	}




}