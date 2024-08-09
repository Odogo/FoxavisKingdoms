package net.foxavis.kingdoms.cmds.sub.admin;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class KAPersistentCommand extends KingdomSubcommand {

	public KAPersistentCommand() {
		super("admin persistent", "Makes a kingdom persistent", "admin persistent <kingdom> [on/off]", "foxavis.kingdoms.admin.persistent");
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(args.length < 1) {
			sender.sendMessage(Component.text("Invalid usage. You must specify a kingdom.", NamedTextColor.RED));
			return true;
		}

		int length = args.length;
		String toggle = "toggle";

		if(args[length - 1].equalsIgnoreCase("on") || args[length - 1].equalsIgnoreCase("off")) {
			toggle = args[length - 1].toLowerCase();
			length--;
		}

		String kName = Stream.of(args).limit(length).reduce((a, b) -> a + " " + b).orElse("");
		Kingdom kingdom = Kingdom.fetchKingdom(kName);
		if(kingdom == null) {
			sender.sendMessage(Component.text("The kingdom " + kName + " does not exist.", NamedTextColor.RED));
			return true;
		}

		if(toggle.equals("toggle")) {
			kingdom.setPersistent(!kingdom.isPersistent());
		} else {
			kingdom.setPersistent(toggle.equals("on"));
		}

		sender.sendMessage(Component.text("The kingdom " + kName + " is now " + (kingdom.isPersistent() ? "persistent" : "not persistent") + ".", NamedTextColor.GREEN));
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(args.length == 0) return null;
		if(args.length == 1) {
			return Kingdom.getKingdomIndex().values().stream()
					.filter(name -> name.startsWith(String.join(" ", args)))
					.map(name -> name.split(" ")[args.length - 1])
					.toList();
		}

		String kName = Stream.of(args).limit(args.length - 1).reduce((a, b) -> a + " " + b).orElse("");
		Kingdom kingdom = Kingdom.fetchKingdom(kName);
		if(kingdom == null) {
			return Kingdom.getKingdomIndex().values().stream()
					.filter(name -> name.startsWith(kName))
					.map(name -> name.split(" ")[args.length - 1])
					.toList();
		} else {
			return List.of("on", "off");
		}
	}
}