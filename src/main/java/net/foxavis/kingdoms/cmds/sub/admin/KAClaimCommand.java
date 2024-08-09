package net.foxavis.kingdoms.cmds.sub.admin;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KAClaimCommand extends KingdomSubcommand {

	public KAClaimCommand() {
		super("admin claim", "Claim a chunk for a kingdom", "admin claim <kingdom>", "foxavis.kingdoms.admin.claim");
	}

	// Grab the kingdom by combining the arguments, the name will be enclosed in quotes always.
	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(!(sender instanceof Player player)) {
			sender.sendMessage(Component.text("You must be an in-game player to use this command.").color(NamedTextColor.RED));
			return true;
		}

		if(args.length == 0) {
			sender.sendMessage(Component.text("You must specify a kingdom to claim the chunk for.").color(NamedTextColor.RED));
			return true;
		}

		Kingdom kingdom = Kingdom.fetchKingdom(String.join(" ", args));
		if(kingdom == null) {
			sender.sendMessage(Component.text("The kingdom specified does not exist.").color(NamedTextColor.RED));
			return true;
		}

		Chunk chunk = player.getChunk();
		Kingdom chunkK = Kingdom.fetchKingdom(chunk);
		if(chunkK == null) {
			kingdom.claimTerritory(player.getChunk());
			player.sendMessage(Component.text("You have claimed this chunk for " + kingdom.getName() + ".").color(NamedTextColor.GREEN));
			return true;
		}

		if(chunkK.equals(kingdom)) {
			sender.sendMessage(Component.text("This chunk is already claimed by " + kingdom.getName() + ".").color(NamedTextColor.RED));
			return true;
		}

		sender.sendMessage(Component.text("This chunk is already claimed by " + chunkK.getName() + ".").color(NamedTextColor.RED));
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		return Kingdom.getKingdomIndex().values().stream()
				.filter(name -> name.startsWith(String.join(" ", args)))
				.map(name -> name.split(" ")[args.length - 1])
				.toList();
	}
}