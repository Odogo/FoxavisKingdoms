package net.foxavis.kingdoms.cmds.sub.d;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.enums.KingdomPerms;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KUnclaimCommand extends KingdomSubcommand {

	public KUnclaimCommand() {
		super("unclaim", "Unclaim chunk(s) from your kingdom", "unclaim <one,square <radius>,all>", null);
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
			player.sendMessage(Component.text("-- /k unclaim Help --").color(NamedTextColor.GOLD));
			player.sendMessage(Component.text("/k unclaim one").color(NamedTextColor.YELLOW).append(Component.text(" - Claim the chunk you are standing in.").color(NamedTextColor.GRAY)));
			player.sendMessage(Component.text("/k unclaim square <radius>").color(NamedTextColor.YELLOW).append(Component.text(" - Claim a square area of chunks.").color(NamedTextColor.GRAY)));
			player.sendMessage(Component.text("/k unclaim all").color(NamedTextColor.YELLOW).append(Component.text(" - Unclaim all chunks in your kingdom.").color(NamedTextColor.GRAY)));
			return true;
		}

		Chunk chunk = player.getChunk();

		if(args[0].equalsIgnoreCase("one")) {
			Kingdom claim = Kingdom.fetchKingdom(chunk);
			if(claim == null || !claim.equals(kingdom)) {
				player.sendMessage(Component.text("This chunk is not claimed by your kingdom.").color(NamedTextColor.RED));
				return true;
			}

			kingdom.unclaimTerritory(chunk);
			player.sendMessage(Component.text("Successfully unclaimed this chunk.").color(NamedTextColor.GREEN));

			kingdom.sendKingdomMessage(
					Component.text(player.getName()).color(NamedTextColor.YELLOW)
							.append(Component.text(" has unclaimed a chunk for the kingdom at ").color(NamedTextColor.GRAY))
							.append(Component.text(chunk.getX() + ", " + chunk.getZ()).color(NamedTextColor.YELLOW))

			);
		} else if(args[0].equalsIgnoreCase("all")) {
			kingdom.unclaimAllTerritory();
			player.sendMessage(Component.text("Successfully unclaimed all chunks in your kingdom.").color(NamedTextColor.GREEN));

			kingdom.sendKingdomMessage(
					Component.text(player.getName()).color(NamedTextColor.YELLOW)
							.append(Component.text(" has unclaimed all chunks for the kingdom.").color(NamedTextColor.GRAY))
			);
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

			if(radius > 5) {
				player.sendMessage(Component.text("The radius must be less than 6.").color(NamedTextColor.RED));
				player.sendMessage(Component.text("This is due to performance reasons and prevent abuse of the command.").color(NamedTextColor.GRAY));
				return true;
			}

			for(int x = -radius; x <= radius; x++) {
				for(int z = -radius; z <= radius; z++) {
					Chunk claim = player.getWorld().getChunkAt(chunk.getX() + x, chunk.getZ() + z);
					Kingdom claimKingdom = Kingdom.fetchKingdom(claim);
					if(claimKingdom != null && !claimKingdom.equals(kingdom)) continue;
					kingdom.unclaimTerritory(claim);
				}
			}

			player.sendMessage(Component.text("You have unclaimed a square area of chunks for your kingdom.").color(NamedTextColor.GREEN));

			kingdom.sendKingdomMessage(
					Component.text(player.getName()).color(NamedTextColor.YELLOW)
							.append(Component.text(" has unclaimed a square area of chunks for the kingdom near ").color(NamedTextColor.GRAY))
							.append(Component.text(chunk.getX() + ", " + chunk.getZ()).color(NamedTextColor.YELLOW))
			);
			return true;
		} else {
			player.sendMessage(Component.text("Invalid argument. Use '/k unclaim' for help.").color(NamedTextColor.RED));
		}
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(args.length == 1) return List.of("one", "square", "all");
		if(args.length == 2 && args[0].equalsIgnoreCase("square")) return List.of("<radius>");
		return List.of();
	}
}