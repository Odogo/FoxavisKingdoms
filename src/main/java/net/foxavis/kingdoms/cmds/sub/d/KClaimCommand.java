package net.foxavis.kingdoms.cmds.sub.d;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.enums.KingdomPerms;
import net.foxavis.kingdoms.objects.KingdomPlayer;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KClaimCommand extends KingdomSubcommand {

	public KClaimCommand() {
		super("claim", "Claim chunk(s) for your kingdom", "claim <one,auto,square <radius>>", null);
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(!(sender instanceof Player player)) {
			sender.sendMessage(Component.text("You must be an in-game player to use this command.").color(NamedTextColor.RED));
			return true;
		}

		Kingdom kingdom = Kingdom.fetchKingdom(player);
		if(kingdom == null) {
			player.sendMessage(Component.text("You must be a member of a kingdom to use this command.").color(NamedTextColor.RED));
			return true;
		}

		if(!kingdom.hasPermission(player, KingdomPerms.CMD_TERRITORY)) {
			player.sendMessage(Component.text("You do not have permission to manage the territory of your kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if(args.length == 0) {
			player.sendMessage(Component.text("-- /k claim Help --").color(NamedTextColor.GOLD));
			player.sendMessage(Component.text("/k claim one").color(NamedTextColor.YELLOW).append(Component.text(" - Claim the chunk you are standing in.").color(NamedTextColor.GRAY)));
			player.sendMessage(Component.text("/k claim auto").color(NamedTextColor.YELLOW).append(Component.text(" - Automatically claim the chunk you are standing in.").color(NamedTextColor.GRAY)));
			player.sendMessage(Component.text("/k claim square <radius>").color(NamedTextColor.YELLOW).append(Component.text(" - Claim a square area of chunks.").color(NamedTextColor.GRAY)));
			return true;
		}

		Chunk chunk = player.getChunk();

		if(args[0].equalsIgnoreCase("one")) {
			Kingdom claim = Kingdom.fetchKingdom(chunk);
			if(claim != null && claim.equals(kingdom)) {
				player.sendMessage(Component.text("This chunk is already claimed by your kingdom.").color(NamedTextColor.RED));
				return true;
			}

			if(claim != null && !claim.equals(chunk)) {
				player.sendMessage(Component.text("This chunk is already claimed by another kingdom.").color(NamedTextColor.RED));
				return true;
			}

			kingdom.claimTerritory(chunk);
			player.sendMessage(Component.text("You have claimed this chunk for your kingdom.").color(NamedTextColor.GREEN));

			kingdom.sendKingdomMessage(
					Component.text(player.getName()).color(NamedTextColor.YELLOW)
							.append(Component.text(" has claimed a chunk for the kingdom at ").color(NamedTextColor.GRAY))
							.append(Component.text(chunk.getX() + ", " + chunk.getZ()).color(NamedTextColor.YELLOW))

			);
			return true;
		} else if(args[0].equalsIgnoreCase("square")) {
			if(args.length < 2) {
				player.sendMessage(Component.text("You must specify a radius for the square area.").color(NamedTextColor.RED));
				return true;
			}

			int radius;
			try {
				radius = Integer.parseInt(args[1]);
			} catch(NumberFormatException e) {
				player.sendMessage(Component.text("The radius must be a number.").color(NamedTextColor.RED));
				return true;
			}

			if(radius < 1) {
				player.sendMessage(Component.text("The radius must be greater than 0.").color(NamedTextColor.RED));
				return true;
			}

			if(radius > 10) {
				player.sendMessage(Component.text("The radius must be less than 11.").color(NamedTextColor.RED));
				player.sendMessage(Component.text("This is due to performance reasons and prevent abuse of the command.").color(NamedTextColor.GRAY));
				return true;
			}

			for(int x = -radius; x <= radius; x++) {
				for(int z = -radius; z <= radius; z++) {
					Chunk claim = player.getWorld().getChunkAt(chunk.getX() + x, chunk.getZ() + z);
					Kingdom claimKingdom = Kingdom.fetchKingdom(claim);
					if(claimKingdom != null && claimKingdom.equals(kingdom)) continue;
					if(claimKingdom != null && !claimKingdom.equals(kingdom)) continue;
					kingdom.claimTerritory(claim);
				}
			}

			player.sendMessage(Component.text("You have claimed a square area of chunks for your kingdom.").color(NamedTextColor.GREEN));

			kingdom.sendKingdomMessage(
					Component.text(player.getName()).color(NamedTextColor.YELLOW)
							.append(Component.text(" has claimed a square area of chunks for the kingdom near ").color(NamedTextColor.GRAY))
							.append(Component.text(chunk.getX() + ", " + chunk.getZ()).color(NamedTextColor.YELLOW))
			);
			return true;
		} else if(args[0].equalsIgnoreCase("auto")) {
			KingdomPlayer kPlayer = KingdomPlayer.fetchPlayer(player);
			if(kPlayer == null) {
				player.sendMessage(Component.text("An error occurred while fetching your player data.").color(NamedTextColor.RED));
				plugin.getLogger().warning("An error occurred while fetching player data for " + player.getName() + ".");
				return true;
			}

			kPlayer.setAutoClaiming(!kPlayer.isAutoClaiming());
			player.sendMessage(Component.text("You have " + (kPlayer.isAutoClaiming() ? "enabled" : "disabled") + " auto-claiming.").color(NamedTextColor.GREEN));
		} else {
			player.sendMessage(Component.text("Invalid argument. Use /k claim for help.").color(NamedTextColor.RED));
		}
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(args.length == 1) return List.of("one", "auto", "square");
		if(args.length == 2 && args[0].equalsIgnoreCase("square")) return List.of("<radius>");
		return List.of();
	}
}