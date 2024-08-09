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

public class KADeleteCommand extends KingdomSubcommand {

	public KADeleteCommand() {
		super("admin delete", "Delete a kingdom", "admin delete <kingdom>", "foxavis.kingdoms.admin.delete");
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(args.length == 0) {
			sender.sendMessage(Component.text("You must specify a kingdom to delete.").color(NamedTextColor.RED));
			return true;
		}

		Kingdom kingdom = Kingdom.fetchKingdom(String.join(" ", args));
		if(kingdom == null) {
			sender.sendMessage(Component.text("The kingdom specified does not exist.").color(NamedTextColor.RED));
			return true;
		}

		kingdom.sendKingdomMessage(Component.text("Your kingdom has been deleted by an admin.").color(NamedTextColor.RED));

		kingdom.delete();
		sender.sendMessage(Component.text("You have deleted the kingdom " + kingdom.getName() + ".").color(NamedTextColor.GREEN));
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		return Kingdom.getKingdomIndex().values().stream()
				.filter(name -> name.startsWith(String.join(" ", args)))
				.map(name -> name.split(" ")[args.length - 1])
				.toList();
	}
}