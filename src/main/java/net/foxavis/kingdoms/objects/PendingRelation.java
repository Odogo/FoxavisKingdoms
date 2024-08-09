package net.foxavis.kingdoms.objects;

import net.foxavis.kingdoms.enums.KingdomRelation;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.foxavis.kingdoms.util.CachedDataManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PendingRelation {

	private static final long expirationTime = TimeUnit.MINUTES.toMillis(10);

	private static final List<PendingRelation> pendingRelations = new ArrayList<>();

	public static List<PendingRelation> getPendingRelations() { return pendingRelations; }
	@Nullable public static PendingRelation getPendingRelation(Kingdom kingdom, Kingdom target, KingdomRelation relation) {
		return pendingRelations.stream()
				.filter(pending -> pending.getRequestingKingdomId().equals(kingdom.getKingdomId()))
				.filter(pending -> pending.getTargetKingdomId().equals(target.getKingdomId()))
				.filter(pending -> pending.getRelation() == relation)
				.findFirst()
				.orElse(null);
	}
	public static boolean hasPendingRelation(Kingdom kingdom, Kingdom target, KingdomRelation relation) {
		return getPendingRelation(kingdom, target, relation) != null;
	}

	public static void addPendingRelation(PendingRelation relation) { pendingRelations.add(relation); }
	public static void removePendingRelation(PendingRelation relation) { pendingRelations.remove(relation); }

	private final UUID requestingKingdom;
	private final UUID targetKingdom;

	private final KingdomRelation relation; // only "ally" and "truce" is allowed
	private final long requestTime;

	public PendingRelation(@NotNull UUID requestingKingdom, @NotNull UUID targetKingdom, KingdomRelation relation) {
		this.requestingKingdom = requestingKingdom;
		this.targetKingdom = targetKingdom;
		this.relation = relation;

		this.requestTime = System.currentTimeMillis();
	}

	public UUID getRequestingKingdomId() { return requestingKingdom; }
	@Nullable
	public Kingdom getRequestingKingdom() { return Kingdom.fetchKingdom(requestingKingdom); }

	public UUID getTargetKingdomId() { return targetKingdom; }
	@Nullable public Kingdom getTargetKingdom() { return Kingdom.fetchKingdom(targetKingdom); }

	public KingdomRelation getRelation() { return relation; }

	public long getRequestTime() { return requestTime; }
	public String getRequestAgo() { return formatTime(System.currentTimeMillis() - requestTime); }

	public boolean isExpired() { return System.currentTimeMillis() - requestTime > expirationTime; }

	private String formatTime(long time) {
		long current = System.currentTimeMillis();
		long diff = time - current;

		long days = TimeUnit.MILLISECONDS.toDays(diff);
		long hours = TimeUnit.MILLISECONDS.toHours(diff) - TimeUnit.DAYS.toHours(days);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) - TimeUnit.HOURS.toMinutes(hours) - TimeUnit.DAYS.toMinutes(days);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(diff) - TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.DAYS.toSeconds(days);

		StringBuilder builder = new StringBuilder();
		if(days > 0) builder.append(days).append("d ");
		if(hours > 0) builder.append(hours).append("h ");
		if(minutes > 0) builder.append(minutes).append("m ");
		builder.append(seconds).append("s");

		return builder.toString();
	}
}