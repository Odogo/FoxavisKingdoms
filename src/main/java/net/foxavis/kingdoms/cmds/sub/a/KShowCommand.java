package net.foxavis.kingdoms.cmds.sub.a;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.enums.KingdomRank;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class KShowCommand extends KingdomSubcommand {

	public KShowCommand() {
		super("show", "Show info about a kingdom", "show <kingdom>", null);
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		Kingdom kingdom = Kingdom.fetchKingdom(String.join(" ", args));
		if (kingdom == null) {
			sender.sendMessage("That kingdom does not exist.");
			return true;
		}

		sender.sendMessage(Component.text("-- Kingdom: ").color(NamedTextColor.GOLD).append(Component.text(kingdom.getName() + " ").color(NamedTextColor.YELLOW)).append(kingdom.getTag().colorIfAbsent(NamedTextColor.GRAY).append(Component.text(" --").color(NamedTextColor.GOLD))));
		sender.sendMessage(Component.text("Description: ").color(NamedTextColor.GOLD).append(Component.text(kingdom.getDescription() != null ? kingdom.getDescription() : "None set").color(NamedTextColor.WHITE)));
		sender.sendMessage(Component.text("Power: ").color(NamedTextColor.GOLD).append(Component.text(kingdom.getPower() + "/" + kingdom.getMaxPower()).color(NamedTextColor.WHITE)));
		sender.sendMessage(Component.text("Members: ").color(NamedTextColor.GOLD).append(Component.text(kingdom.getAllOnlineMembers().size() + "/" + kingdom.getMembers().size()).color(NamedTextColor.WHITE)));
		for(KingdomRank rank : new KingdomRank[] { KingdomRank.LEADER, KingdomRank.OFFICER, KingdomRank.MEMBER, KingdomRank.RECRUIT }) {
			List<OfflinePlayer> rankMembers = kingdom.getMembersByRank(rank);
			if(rankMembers.isEmpty()) continue;

			Component rankComponent = Component.text("- " + rank.getName() + ": ").color(NamedTextColor.GOLD);
			rankComponent = rankComponent.append(Component.join(JoinConfiguration.separator(Component.text(", ").color(NamedTextColor.GRAY)), rankMembers.stream().map((op) -> {
				String name = Objects.requireNonNull(op.getName());
				boolean isOnline = op.isOnline();
				return Component.text(name).color(isOnline ? NamedTextColor.GREEN : NamedTextColor.RED);
			}).toList()));
			sender.sendMessage(rankComponent);
		}
		sender.sendMessage(Component.text("Allies: ").color(NamedTextColor.GOLD).append(
				kingdom.getAllies().isEmpty() ? Component.text("None").color(NamedTextColor.GRAY) :
						Component.join(JoinConfiguration.separator(Component.text(", ").color(NamedTextColor.GRAY)), kingdom.getAllies().stream().map(Kingdom::getName).map(a -> Component.text(a).color(NamedTextColor.LIGHT_PURPLE)).toList())
		));
		sender.sendMessage(Component.text("Enemies: ").color(NamedTextColor.GOLD).append(
				kingdom.getEnemies().isEmpty() ? Component.text(	"None").color(NamedTextColor.GRAY) :
						Component.join(JoinConfiguration.separator(Component.text(", ").color(NamedTextColor.GRAY)), kingdom.getEnemies().stream().map(Kingdom::getName).map(a -> Component.text(a).color(NamedTextColor.RED)).toList())
		));
		sender.sendMessage(Component.text("-- Kingdom: ").color(NamedTextColor.GOLD).append(Component.text(kingdom.getName() + " ").color(NamedTextColor.YELLOW)).append(kingdom.getTag().colorIfAbsent(NamedTextColor.GRAY).append(Component.text(" --").color(NamedTextColor.GOLD))));
		return true;

	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		return Kingdom.getKingdomIndex().values().stream()
				.filter(name -> name.startsWith(String.join(" ", args)))
				.map(name -> name.split(" ")[args.length - 1])
				.toList();
	}
}
