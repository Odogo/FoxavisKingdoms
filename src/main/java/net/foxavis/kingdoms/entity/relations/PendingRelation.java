package net.foxavis.kingdoms.entity.relations;

import net.foxavis.kingdoms.entity.KingdomHeld;
import net.foxavis.kingdoms.entity.primary.Kingdom;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * This class represents a pending relationship between two kingdoms. Pending Relations can only exist for Ally and Trust relationships.
 * <p>
 *     When a kingdom sends a request to another kingdom to form an alliance or a truce, a PendingRelation is created.
 *     This PendingRelation will be stored in the requesting kingdom's data until the target kingdom accepts or denies the request or the request expires.
 * </p>
 * @see KingdomRelation
 * @see Kingdom
 * @see KingdomHeld
 * @author Kyomi
 */
public class PendingRelation {

	public static final long EXPIRATION_TIME = TimeUnit.DAYS.toMillis(7);

	private final @NotNull UUID requestingKingdom; // the sending kingdom ID
	private final @NotNull UUID targetKingdom; // the target kingdom ID

	private final @NotNull KingdomRelation requestingRelation; // The relationship being requested
	private final long sentAt; // The time when this request was sent

	public PendingRelation(@NotNull Kingdom requesting, @NotNull Kingdom target, @NotNull KingdomRelation requestingRelation) {
		//this.requestingKingdom = requesting;
		//this.targetKingdom = target;
		this.requestingRelation = requestingRelation;
		this.sentAt = System.currentTimeMillis();
	}

	/**
	 * Returns the kingdom that sent the request.
	 * @return The kingdom that sent the request.
	 */
	public @NotNull Kingdom getRequestingKingdom() { return null; } // TODO: Get the kingdom from the ID

	/**
	 * Returns the kingdom that the request was sent to.
	 * @return The kingdom that the request was sent to.
	 */
	public @NotNull Kingdom getTargetKingdom() { return null; } // TODO: Get the kingdom from the ID

	/**
	 * Returns the relationship that is being requested.
	 * @return The relationship that is being requested.
	 */
	public @NotNull KingdomRelation getRequestingRelation() { return requestingRelation; }

	/**
	 * Returns the time when this request was sent.
	 * @return The time when this request was sent.
	 */
	public long getSentAt() { return sentAt; }

	/**
	 * Returns whether this request has expired.
	 * @return Whether this request has expired.
	 */
	public boolean isExpired() { return System.currentTimeMillis() - sentAt > EXPIRATION_TIME; }

	@Override public String toString() {
		return "PendingRelation{" +
			   "requestingKingdom=" + requestingKingdom +
			   ", targetKingdom=" + targetKingdom +
			   ", requestingRelation=" + requestingRelation +
			   ", sentAt=" + sentAt +
			   '}';
	}

	@Override public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PendingRelation that)) return false;

		return sentAt == that.sentAt &&
			   requestingKingdom.equals(that.requestingKingdom) &&
			   targetKingdom.equals(that.targetKingdom) &&
			   requestingRelation == that.requestingRelation;
	}

	@Override public int hashCode() {
		int result = requestingKingdom.hashCode();
		result = 31 * result + targetKingdom.hashCode();
		result = 31 * result + requestingRelation.hashCode();
		result = 31 * result + Long.hashCode(sentAt);
		return result;
	}
}