package net.foxavis.kingdoms.cmds.sub.admin;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KAUnclaimCommand extends KingdomSubcommand {

	public KAUnclaimCommand() {
		super("admin unclaim", "Unclaim a chunk for a kingdom", "admin unclaim", "foxavis.kingdoms.admin.unclaim");
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(!(sender instanceof Player player)) {
			sender.sendMessage(Component.text("You must be an in-game player to use this command.").color(NamedTextColor.RED));
			return true;
		}

		if(args.length == 0) {
			player.sendMessage(Component.text("You must specify a kingdom to unclaim the chunk for.").color(NamedTextColor.RED));
			return true;
		}

		Kingdom kingdom = Kingdom.fetchKingdom(player.getChunk());
		if(kingdom == null) {
			player.sendMessage(Component.text("This chunk is not claimed by any kingdom.").color(NamedTextColor.RED));
			return true;
		}

		kingdom.unclaimTerritory(player.getChunk());
		player.sendMessage(Component.text("You have unclaimed this chunk for " + kingdom.getName() + ".").color(NamedTextColor.GREEN));

		kingdom.sendKingdomMessage(Component.text("An admin has unclaimed a chunk for the kingdom.").color(NamedTextColor.RED));
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		return List.of();
	}
}