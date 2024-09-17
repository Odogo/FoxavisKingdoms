package net.foxavis.kingdoms.entity;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * This class is defining the ranks within a Kingdom. Each kingdom gets 4 default ranks that every kingdom comes with.
 * <p>
 *     Members with permission to modify ranks can add/remove their own ranks or modify the default ones given. However, one rank must be defined in the kingdom.
 * </p>
 * @author Kyomi
 */
public class KingdomRank implements Describable {

    /** The default Recruit rank, generated for all newly created Kingdoms */
    public static final KingdomRank DEFAULT_RECRUIT = new KingdomRank(
            3,
            "Recruit",
            "Fresh recruits of the kingdom, has not a lot of authority",
            true,
            Component.text("Recruit").color(NamedTextColor.YELLOW)
    );

    /** The default Member rank, generated for all newly created Kingdoms */
    public static final KingdomRank DEFAULT_MEMBER = new KingdomRank(
            2,
            "Member",
            "Respected members; has more authority but not drastic changed",
            false,
            Component.text("Member").color(NamedTextColor.GOLD)
    );

    /** The default Officer rank, generated for all newly created Kingdoms */
    public static final KingdomRank DEFAULT_OFFICER = new KingdomRank(
            1,
            "Officer",
            "High ranking officers; has a lot of authority within",
            false,
            Component.text("Officer").color(NamedTextColor.RED)
    );

    /** The default Leader rank, generated for all newly created Kingdoms */
    public static final KingdomRank DEFAULT_LEADER = new KingdomRank(
            0,
            "Leader",
            "The highest member within the kingdom",
            false,
            Component.text("Leader").color(NamedTextColor.BLUE),
            KingdomPerm.values()
    );

    private int rankId; // the unique identifier for the rank, unique for each kingdom
    @NotNull private String name; // the name of the rank
    @Nullable private String description; // the optional description of a rank

    private boolean isDefault; // if this rank is a default rank

    @Nullable private String rawPrefix; // the prefix, in its deserialized form, for the rank

    @NotNull private final List<KingdomPerm> permissions; // the permissions for the rank

    /**
     * Creates a new KingdomRank with all parameters, including an infinite list for permissions.
     * @param rankId The unique identifier for the rank (unique for each kingdom)
     * @param name The name of the rank
     * @param prefix The optional display symbol of the rank. If none is set, the first letter of the rank name will be displayed instead.
     * @param description The optional description of the rank (set to null for no description)
     * @param permissions An array/varargs of permissions that are assigned to this rank.
     */
    @ApiStatus.Internal
    KingdomRank(final int rankId, @NotNull String name, @Nullable String description, boolean isDefault, @Nullable TextComponent prefix, KingdomPerm... permissions) {
        this.rankId = rankId;
        this.name = name;
        this.description = description;

        this.isDefault = isDefault;

        this.rawPrefix = prefix != null ? LegacyComponentSerializer.legacyAmpersand().serialize(prefix) : null;

        this.permissions = new ArrayList<>(List.of(permissions));
    }

    /**
     * Creates a new KingdomRank with all parameters, including an infinite list for permissions.
     * @param rankId The unique identifier for the rank (unique for each kingdom)
     * @param name The name of the rank
     * @param description The optional description of the rank (set to null for no description)
     * @param isDefault If this rank is a default rank
     * @param prefix The optional display symbol of the rank. If none is set, the first letter of the rank name will be displayed instead.
     * @param permissions A collection of permissions that are assigned to this rank.
     */
    KingdomRank(final int rankId, @NotNull String name, @Nullable String description, boolean isDefault, @Nullable TextComponent prefix, @NotNull Collection<KingdomPerm> permissions) {
        this.rankId = rankId;
        this.name = name;
        this.description = description;

        this.isDefault = isDefault;

        this.rawPrefix = prefix != null ? LegacyComponentSerializer.legacyAmpersand().serialize(prefix) : null;

        this.permissions = new ArrayList<>(permissions);
    }

    /**
     * Returns the current rank ID of the rank
     * @return The rank ID of the rank
     */
    public int getRankId() { return rankId; }

    @Override public @NotNull String getName() { return name; }
    @Override public void setName(@NotNull String name) { this.name = name; }

    @Override public @Nullable String getDescription() { return description; }
    @Override public void setDescription(@Nullable String description) { this.description = description; }

    /**
     * Returns the raw prefix of the rank, if any.
     * <p>
     *     This is the deserialize form of the prefix, and is used to store the prefix into the storage system.
     *     This is not the display form of the prefix. To get the display form, use {@link #getPrefix()}.
     *     If no prefix is set, this will return null.
     * </p>
     * @return The raw prefix, otherwise null if none set
     */
    @Nullable public String getRawPrefix() { return rawPrefix; }

    /**
     * Returns the stored display symbol of the rank, if any
     * @return The character for the symbol, otherwise null if none set
     */
    @Nullable public TextComponent getPrefix() { return (rawPrefix != null ? LegacyComponentSerializer.legacyAmpersand().deserialize(rawPrefix) : null); }

    /**
     * Sets the stored display symbol of the rank. This will be displayed (instead of the first letter, if none set)
     * @param prefix The new symbol to display
     */
    public void setPrefix(@Nullable TextComponent prefix) { this.rawPrefix = (prefix != null ? LegacyComponentSerializer.legacyAmpersand().serialize(prefix) : null); }

    /**
     * Returns all the permissions that this rank grants to members
     * @return A list of permissions this rank grants
     */
    public List<KingdomPerm> getAllPermissions() { return permissions; }

    /**
     * Checks if this rank grants the given permission
     * @param perm The permission to check
     * @return True if this rank grants this permission, false if it doesn't.
     * @see Collection#contains(Object)
     */
    public boolean hasPermission(KingdomPerm perm) { return permissions.contains(perm); }

    /**
     * Adds a single permission that this rank will grant.
     * @param perm The new permission to grant.
     */
    public void addPermission(KingdomPerm perm) { permissions.add(perm); }

    /**
     * Adds several permissions that this rank will grant.
     * @param perms An array of permissions to add
     */
    public void addPermissions(KingdomPerm... perms) { permissions.addAll(List.of(perms)); }

    /**
     * Adds several permissions that this rank will grant
     * @param perms A collection of permissions to add
     */
    public void addPermissions(Collection<KingdomPerm> perms) { permissions.addAll(perms); }

    /**
     * Removes a single permission that this rank would grant.
     * @param perm The permission to revoke
     */
    public void revokePermission(KingdomPerm perm) { permissions.remove(perm); }

    /**
     * Removes multiple permissions that this rank would grant
     * @param perms An array of permissions to revoke
     */
    public void revokePermission(KingdomPerm... perms) { permissions.removeAll(List.of(perms)); }

    /**
     * Removes multiple permissions that this rank would grant
     * @param perms A collection of permissions to revoke
     */
    public void revokePermission(Collection<KingdomPerm> perms) { permissions.removeAll(perms); }

    @Override public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KingdomRank that)) return false;

		return rankId == that.rankId &&
               isDefault == that.isDefault &&
               name.equals(that.name) &&
               Objects.equals(description, that.description) &&
               Objects.equals(rawPrefix, that.rawPrefix) &&
               permissions.equals(that.permissions);
    }

    @Override public int hashCode() {
        int result = rankId;
        result = 31 * result + name.hashCode();
        result = 31 * result + Objects.hashCode(description);
        result = 31 * result + Boolean.hashCode(isDefault);
        result = 31 * result + Objects.hashCode(rawPrefix);
        result = 31 * result + permissions.hashCode();
        return result;
    }
}