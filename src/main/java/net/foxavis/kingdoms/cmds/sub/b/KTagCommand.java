package net.foxavis.kingdoms.cmds.sub.b;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.enums.KingdomPerms;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.foxavis.kingdoms.util.KingdomException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KTagCommand extends KingdomSubcommand {

	public KTagCommand() {
		super("tag", "Set the tag of your kingdom", "tag <tag>", null);
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

		if(!kingdom.hasPermission(player, KingdomPerms.CMD_TAG)) {
			player.sendMessage(Component.text("You do not have permission to change the tag of your kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if(args.length == 0) {
			player.sendMessage(Component.text("You must specify a new tag for your kingdom.").color(NamedTextColor.RED));
			return true;
		}

		TextComponent component = LegacyComponentSerializer.legacyAmpersand().deserialize(String.join(" ", args));
		try {
			kingdom.setTag(component);
			player.sendMessage(
					Component.text("You have changed the tag of your kingdom to ").color(NamedTextColor.GREEN).append(
							component.colorIfAbsent(NamedTextColor.GRAY)
					).append(Component.text(".").color(NamedTextColor.GREEN)
			));
		} catch (KingdomException e) {
			player.sendMessage(Component.text(e.getMessage()).color(NamedTextColor.RED));
		}
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		return List.of();
	}
}