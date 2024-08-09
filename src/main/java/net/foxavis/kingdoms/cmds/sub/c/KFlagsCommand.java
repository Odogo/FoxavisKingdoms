package net.foxavis.kingdoms.cmds.sub.c;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.enums.KingdomFlags;
import net.foxavis.kingdoms.enums.KingdomPerms;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class KFlagsCommand extends KingdomSubcommand {

	public KFlagsCommand() {
		super("flags", "View or set kingdom flags", "flags [<flag> [<value>]]", null);
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

		if(!kingdom.hasPermission(player, KingdomPerms.CMD_FLAGS)) {
			player.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
			return true;
		}

		if(args.length == 0) {
			player.sendMessage(Component.text("The flags menu is not yet implemented. Please use /k flags <flag> <value> to set a flag!").color(NamedTextColor.RED));
			return true;
		}

		try {
			KingdomFlags flag = KingdomFlags.valueOf(args[0].toUpperCase());
			if(args.length == 1) {
				player.sendMessage(Component.text("The value of the " + flag.name() + " flag is " + (kingdom.getFlag(flag) ? "ALLOWED": "DENIED") + ".").color(NamedTextColor.GREEN));
			} else {
				String arg = args[1].toLowerCase();
				boolean value = (arg.equals("true") || arg.equals("allow") || arg.equals("allowed"));
				kingdom.setFlag(flag, value);
				player.sendMessage(Component.text("The value of the " + flag.name() + " flag has been set to " + (value ? "ALLOWED": "DENIED") + ".").color(NamedTextColor.GREEN));
			}
		} catch(IllegalArgumentException e) {
			player.sendMessage(Component.text("That flag does not exist.").color(NamedTextColor.RED));
		}
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(args.length == 1) return Stream.of(KingdomFlags.values()).map(Enum::name).filter(name -> name.startsWith(args[0].toUpperCase())).toList();
		if(args.length == 2) return Stream.of("true", "allowed", "allow", "false", "denied", "deny").filter(name -> name.startsWith(args[1])).toList();
		return List.of();
	}
}
