package net.foxavis.kingdoms.cmds.sub.b;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.enums.KingdomPerms;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.foxavis.kingdoms.util.KingdomException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KNameCommand extends KingdomSubcommand {

	public KNameCommand() {
		super("name", "Change the name of your kingdom", "name <name>", null);
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(!(sender instanceof Player player)) {
			sender.sendMessage(Component.text("You must be an in-game player to use this command.").color(NamedTextColor.RED));
			return true;
		}

		Kingdom kingdom = Kingdom.fetchKingdom(player);
		if(kingdom == null) {
			player.sendMessage(Component.text("You are not a member of a kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if(!kingdom.hasPermission(player, KingdomPerms.CMD_NAME)) {
			player.sendMessage(Component.text("You do not have permission to change the name of your kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if(args.length == 0) {
			player.sendMessage(Component.text("You must specify a new name for your kingdom.").color(NamedTextColor.RED));
			return true;
		}

		String prevName = kingdom.getName();
		String name = String.join(" ", args);

		try {
			kingdom.renameKingdom(name);
			player.sendMessage(Component.text("You have renamed your kingdom to " + kingdom.getName() + ".").color(NamedTextColor.GREEN));
			kingdom.sendKingdomMessage(Component.text(player.getName() + " has renamed the kingdom to " + kingdom.getName() + ".").color(NamedTextColor.YELLOW));

			plugin.getServer().broadcast(Component.text(prevName + " has been renamed to " + kingdom.getName() + ".").color(NamedTextColor.YELLOW));
		} catch (KingdomException e) {
			player.sendMessage(Component.text(e.getMessage()).color(NamedTextColor.RED));
		}
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		return List.of("<name>");
	}
}