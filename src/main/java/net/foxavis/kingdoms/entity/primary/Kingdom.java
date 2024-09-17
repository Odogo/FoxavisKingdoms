package net.foxavis.kingdoms.entity.primary;

import net.foxavis.kingdoms.entity.KingdomFlag;
import net.foxavis.kingdoms.entity.KingdomRank;
import net.foxavis.kingdoms.entity.locations.KingdomChunk;
import net.foxavis.kingdoms.entity.locations.KingdomLocation;
import net.foxavis.kingdoms.entity.taxes.TaxRate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class Kingdom { //implements Describable, Territorial {

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
	private TaxRate taxRate; // The tax rate of the Kingdom

	// -- Relations -- \\




}