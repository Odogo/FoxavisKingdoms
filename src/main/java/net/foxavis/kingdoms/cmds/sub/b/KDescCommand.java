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
import java.util.logging.Level;

public class KDescCommand extends KingdomSubcommand {

	public KDescCommand() {
		super("desc", "Change the description of your kingdom", "desc <description>", null);
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

		if(!kingdom.hasPermission(player, KingdomPerms.CMD_DESCRIPTION)) {
			player.sendMessage(Component.text("You do not have permission to change the description of your kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if(args.length == 0) {
			player.sendMessage(Component.text("You must specify a description for your kingdom.").color(NamedTextColor.RED));
			return true;
		}

		String description = String.join(" ", args);

		try {
			kingdom.setDescription(description);
			player.sendMessage(Component.text("You have changed the description of your kingdom to:").color(NamedTextColor.GREEN));
			player.sendMessage(Component.text(description).color(NamedTextColor.YELLOW));
		} catch (KingdomException e) {
			player.sendMessage(Component.text(e.getMessage()).color(NamedTextColor.RED));
		}
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		return List.of("<description>");
	}
}
