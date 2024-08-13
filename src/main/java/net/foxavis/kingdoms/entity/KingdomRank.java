package net.foxavis.kingdoms.entity;

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

	@NotNull private String name; // the name of the rank
	@Nullable private String description; // the optional description of a rank

	@Nullable private Character symbol; // an optional display symbol of a rank

	@NotNull private final List<KingdomPerm> permissions; // the permissions for the rank

	/**
	 * Creates a new KingdomRank with all parameters, including an infinite list for permissions.
	 * @param name The name of the rank
	 * @param symbol The optional display symbol of the rank. If none is set, the first letter of the rank name will be displayed instead.
	 * @param description The optional description of the rank (set to null for no description)
	 * @param permissions An array/varargs of permissions that are assigned to this rank.
	 */
	public KingdomRank(@NotNull String name, @Nullable Character symbol, @Nullable String description, KingdomPerm... permissions) {
		this.name = name;
		this.description = description;

		this.symbol = symbol;

		this.permissions = new ArrayList<>(List.of(permissions));
	}

	/**
	 * Creates a new KingdomRank with all parameters with a collection of permissions.
	 * @param name The name of the rank
	 * @param symbol The optional display symbol of the rank. If none is set, the first letter of the rank name will be displayed instead.
	 * @param description The optional description of the rank (set to null for no description)
	 * @param permissions A collection of permissions that are assigned to this rank.
	 */
	public KingdomRank(@NotNull String name, @Nullable Character symbol, @Nullable String description, @NotNull Collection<KingdomPerm> permissions) {
		this.name = name;
		this.description = description;

		this.symbol = symbol;

		this.permissions = new ArrayList<>(permissions);
	}

	/**
	 * Creates a new KingdomRank with all parameters except has no permissions.
	 * @param name The name of the rank
	 * @param symbol The optional display symbol of the rank. If none is set, the first letter of the rank name will be displayed instead.
	 * @param description The optional description of the rank (set to null for no description)
	 */
	public KingdomRank(@NotNull String name, @Nullable Character symbol, @Nullable String description) {
		this.name = name;
		this.description = description;

		this.symbol = symbol;

		this.permissions = new ArrayList<>();
	}

	/**
	 * Creates a new KingdomRank with a name and a symbol, but lacks any permissions or a description.
	 * @param name The name of the rank
	 * @param symbol The optional display symbol of the rank. If none is set, the first letter of the rank name will be displayed instead.
	 */
	public KingdomRank(@NotNull String name, @Nullable Character symbol) { this(name, symbol, null); }

	/**
	 * Creates a blank KingdomRank with only a name and lacks everything else.
	 * @param name The name of the rank
	 */
	public KingdomRank(@NotNull String name) { this(name, null, null); }

	@Override public @NotNull String getName() { return name; }
	@Override public void setName(@NotNull String name) { this.name = name; }

	@Override public @Nullable String getDescription() { return description; }
	@Override public void setDescription(@Nullable String description) { this.description = description; }

	/**
	 * Returns the stored display symbol of the rank, if any
	 * @return The character for the symbol, otherwise null if none set
	 */
	@Nullable public Character getSymbol() { return symbol; }

	/**
	 * Sets the stored display symbol of the rank. This will be displayed (instead of the first letter, if none set)
	 * @param symbol The new symbol to display
	 */
	public void setSymbol(@Nullable Character symbol) { this.symbol = symbol; }

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

		return name.equals(that.name) && Objects.equals(description, that.description) && Objects.equals(symbol, that.symbol) && permissions.equals(that.permissions);
	}

	@Override public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + Objects.hashCode(description);
		result = 31 * result + Objects.hashCode(symbol);
		result = 31 * result + permissions.hashCode();
		return result;
	}
}