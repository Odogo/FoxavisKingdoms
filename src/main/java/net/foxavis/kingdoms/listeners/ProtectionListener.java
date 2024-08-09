package net.foxavis.kingdoms.listeners;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.enums.KingdomFlags;
import net.foxavis.kingdoms.enums.KingdomPerms;
import net.foxavis.kingdoms.enums.KingdomRelation;
import net.foxavis.kingdoms.objects.KingdomPlayer;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.BlockInventoryHolder;

public class ProtectionListener implements Listener {

	private final FoxavisKingdoms plugin;
	public ProtectionListener(FoxavisKingdoms plugin) {
		this.plugin = plugin;
	}

	@EventHandler public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		KingdomPlayer kPlayer = KingdomPlayer.fetchPlayer(player);
		if(kPlayer == null) kPlayer = new KingdomPlayer(player);

		if(kPlayer.isOverriding()) return;

		Kingdom currentKingdom = Kingdom.fetchKingdom(player.getChunk());
		if(currentKingdom == null) return;

		if(currentKingdom.isMember(player)) {
			if(currentKingdom.hasPermission(player, KingdomPerms.WORLD_BUILD)) return;

			event.setCancelled(true);
			player.sendMessage(Component.text("Your kingdom does not grant you permission to break blocks in its territory.").color(NamedTextColor.RED));
		}

		event.setCancelled(true);
		player.sendMessage(Component.text()
				.append(currentKingdom.getTag())
				.append(Component.text(currentKingdom.getName()).color(NamedTextColor.GOLD))
				.append(Component.text(" protects this land. You may not interact with it.").color(NamedTextColor.RED))
		);
	}

	@EventHandler public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		KingdomPlayer kPlayer = KingdomPlayer.fetchPlayer(player);
		if(kPlayer == null) kPlayer = new KingdomPlayer(player);

		if(kPlayer.isOverriding()) return;

		Kingdom currentKingdom = Kingdom.fetchKingdom(player.getChunk());
		if(currentKingdom == null) return;

		if(currentKingdom.isMember(player)) {
			if(currentKingdom.hasPermission(player, KingdomPerms.WORLD_BUILD)) return;

			event.setCancelled(true);
			player.sendMessage(Component.text("Your kingdom does not grant you permission to place blocks in its territory.").color(NamedTextColor.RED));
		}

		event.setCancelled(true);
		player.sendMessage(Component.text()
				.append(currentKingdom.getTag())
				.append(Component.text(currentKingdom.getName()).color(NamedTextColor.GOLD))
				.append(Component.text(" protects this land. You may not interact with it.").color(NamedTextColor.RED))
		);
	}

	@EventHandler public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		KingdomPlayer kPlayer = KingdomPlayer.fetchPlayer(player);
		if(kPlayer == null) kPlayer = new KingdomPlayer(player);

		if(kPlayer.isOverriding()) return;

		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Block block = event.getClickedBlock();
		if(block == null) return;

		if(!isContainerType(block) && !isDoorType(block.getType()) && !isSwitchType(block.getType())) return;

		Kingdom currentKingdom = Kingdom.fetchKingdom(block.getChunk());
		if(currentKingdom == null) return;

		if(currentKingdom.isMember(player)) {
			if(isContainerType(block) && currentKingdom.hasPermission(player, KingdomPerms.WORLD_CONTAINER)) return;
			if(isDoorType(block.getType()) && currentKingdom.hasPermission(player, KingdomPerms.WORLD_DOORS)) return;
			if(isSwitchType(block.getType()) && currentKingdom.hasPermission(player, KingdomPerms.WORLD_SWITCHES)) return;

			event.setCancelled(true);
			player.sendMessage(Component.text("Your kingdom does not grant you permission to interact with this block in its territory.").color(NamedTextColor.RED));
			return;
		}

		event.setCancelled(true);
		player.sendMessage(Component.text()
				.append(currentKingdom.getTag())
				.append(Component.text(currentKingdom.getName()).color(NamedTextColor.GOLD))
				.append(Component.text(" protects this land. You may not interact with it.").color(NamedTextColor.RED))
		);
	}

	@EventHandler public void onPlayerAttackEntity(EntityDamageByEntityEvent event) {
		// Get the damager as a player, using the projectile shooter if necessary.
		Player attackingPlayer;
		if(event.getDamager() instanceof Projectile) {
			if(!(((Projectile) event.getDamager()).getShooter() instanceof Player)) return;
			attackingPlayer = (Player) ((Projectile) event.getDamager()).getShooter();
		} else if(event.getDamager() instanceof Player) {
			attackingPlayer = (Player) event.getDamager();
		} else return; // Not a player or a projectile.

		Kingdom chunkKingdom = Kingdom.fetchKingdom(event.getEntity().getLocation().getChunk());

		if(chunkKingdom != null && chunkKingdom.getFlag(KingdomFlags.PACIFISM)) {
			event.setCancelled(true);
			attackingPlayer.sendMessage(Component.text("This kingdom has pacifism enabled. You may not attack entities within its territory.").color(NamedTextColor.RED));
			return;
		}

		if(!(event.getEntity() instanceof Player target)) return;
		Kingdom targetKingdom = Kingdom.fetchKingdom(target);
		Kingdom attackingKingdom = Kingdom.fetchKingdom(attackingPlayer);

		if(targetKingdom == null || attackingKingdom == null) return;

		if(attackingKingdom.isAlly(targetKingdom)) {
			event.setCancelled(true);
			attackingPlayer.sendMessage(Component.text("You cannot attack ").color(NamedTextColor.RED)
					.append(targetKingdom.getTag())
					.append(Component.text(target.getName()).color(KingdomRelation.ALLY.getPlayerNameColor()))
					.append(Component.text(" as you are formally allies.").color(NamedTextColor.RED)));
		} else if(attackingKingdom.isTruce(targetKingdom)) {
			event.setCancelled(true);
			attackingPlayer.sendMessage(Component.text("You cannot attack ").color(NamedTextColor.RED)
					.append(targetKingdom.getTag())
					.append(Component.text(target.getName()).color(KingdomRelation.TRUCE.getPlayerNameColor()))
					.append(Component.text(" as you are formally in a truce.").color(NamedTextColor.RED)));
		}
	}

	private boolean isDoorType(Material material) {
		return material.name().contains("DOOR") || material.name().contains("GATE");
	}

	private boolean isSwitchType(Material material) {
		return material.name().contains("BUTTON") || material.name().contains("LEVER");
	}

	private boolean isContainerType(Block material) {
		return material.getState() instanceof BlockInventoryHolder;
	}

	private boolean isCreatureProtected(EntityType type) {
		return switch (type) {
			case BAT, CAT, CHICKEN, COD, COW, DONKEY, FOX, HORSE, LLAMA, MUSHROOM_COW, MULE, OCELOT, PANDA, PARROT, PIG, POLAR_BEAR, RABBIT, SALMON, SHEEP, SKELETON_HORSE, SNOWMAN, SQUID,
				 TROPICAL_FISH, TURTLE, VILLAGER, WANDERING_TRADER, WOLF, ZOMBIE_HORSE -> true;
			default -> false;
		};
	}
}
