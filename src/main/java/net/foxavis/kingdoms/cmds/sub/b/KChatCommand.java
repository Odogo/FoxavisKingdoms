package net.foxavis.kingdoms.cmds.sub.b;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.objects.KingdomPlayer;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KChatCommand extends KingdomSubcommand {

	public KChatCommand() {
		super("chat", "Send a message to your kingdom.", "chat <global|ally|kingdom> [<message>]");
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

		if(args.length == 0) {
			player.sendMessage(Component.text("You must specify a chat type.").color(NamedTextColor.RED));
			return true;
		}

		String chatType = args[0];
		if(args.length == 1) {
			try {
				KingdomPlayer.ChatType type = KingdomPlayer.ChatType.valueOf(chatType.toUpperCase());
				KingdomPlayer.setChatType(player, type);
				player.sendMessage(Component.text("Chat type set to " + type.name().toLowerCase() + ".").color(NamedTextColor.GREEN));
			} catch(IllegalArgumentException e) {
				player.sendMessage(Component.text("Invalid chat type. Valid types are: global, ally, kingdom.").color(NamedTextColor.RED));
			}
			return true;
		}

		String message = String.join(" ", args).substring(chatType.length() + 1);

		switch(chatType.toLowerCase()) {
			case "global":
				player.chat(message);
				break;
			case "ally":
				kingdom.sendAllyMessage(player, LegacyComponentSerializer.legacyAmpersand().deserialize(message));
				break;
			case "kingdom":
				kingdom.sendKingdomMessage(player, LegacyComponentSerializer.legacyAmpersand().deserialize(message));
				break;
			default:
				player.sendMessage(Component.text("Invalid chat type. Valid types are: global, ally, kingdom.").color(NamedTextColor.RED));
				break;
		}

		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(args.length == 1) {
			return List.of("global", "ally", "kingdom");
		}

		return List.of();
	}
}
