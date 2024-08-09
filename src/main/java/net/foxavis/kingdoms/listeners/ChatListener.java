package net.foxavis.kingdoms.listeners;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.objects.KingdomPlayer;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

public class ChatListener implements Listener {

	private final FoxavisKingdoms plugin;
	public ChatListener(FoxavisKingdoms plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerChat(AsyncChatEvent event) {
		Player player = event.getPlayer();
		Kingdom kingdom = Kingdom.fetchKingdom(player);

		if(event.isAsynchronous()) {
			if (attemptShorthandedKingdomChat(player, kingdom, event.message())) {
				event.setCancelled(true);
				return;
			}

			if (attemptKingdomChat(player, kingdom, event.message())) {
				event.setCancelled(true);
				return;
			}
		}

		ChatRenderer renderer = event.renderer();
		event.renderer(sendGlobalChat(player, kingdom, renderer));
	}

	private boolean attemptShorthandedKingdomChat(Player player, @Nullable Kingdom kingdom, ComponentLike message) {
		String content = LegacyComponentSerializer.legacyAmpersand().serialize(message.asComponent());
		if(content.startsWith("k:") || content.startsWith("K:")) {
			content = content.substring(2).trim();

			if(kingdom == null) {
				player.sendMessage(Component.text("You must be a member of a kingdom to use kingdom chat.").color(NamedTextColor.RED));
			} else kingdom.sendKingdomMessage(player, LegacyComponentSerializer.legacyAmpersand().deserialize(content));

			return true;
		} else if(content.startsWith("a:") || content.startsWith("A:")) {
			content = content.substring(2).trim();

			if(kingdom == null) {
				player.sendMessage(Component.text("You must be a member of a kingdom to use ally chat.").color(NamedTextColor.RED));
			} else kingdom.sendAllyMessage(player, LegacyComponentSerializer.legacyAmpersand().deserialize(content));

			return true;
		}

		return false;
	}

	private boolean attemptKingdomChat(Player player, @Nullable Kingdom kingdom, ComponentLike message) {
		if(kingdom == null) return false;

		String content = LegacyComponentSerializer.legacyAmpersand().serialize(message.asComponent());
		KingdomPlayer.ChatType chatType = KingdomPlayer.getChatType(player);

		if(chatType == KingdomPlayer.ChatType.KINGDOM) {
			kingdom.sendKingdomMessage(player, LegacyComponentSerializer.legacyAmpersand().deserialize(content));
			return true;
		} else if(chatType == KingdomPlayer.ChatType.ALLY) {
			kingdom.sendAllyMessage(player, LegacyComponentSerializer.legacyAmpersand().deserialize(content));
			return true;
		}

		return false;
	}

	private ChatRenderer sendGlobalChat(Player player, @Nullable Kingdom kingdom, ChatRenderer renderer) {
		return (source, sourceDisplayName, message, viewer) -> {
			if(kingdom == null) return renderer.render(source, sourceDisplayName, message, viewer);
			else {
				Component tag = kingdom.getTag()
						.colorIfAbsent(NamedTextColor.GRAY)
						.hoverEvent(
								Component.text("This player is a ").color(NamedTextColor.GRAY).append(
										Component.text(kingdom.getRank(player).getName()).color(NamedTextColor.YELLOW)
								).append(Component.text(" of ")).append(
										Component.text(kingdom.getName()).color(NamedTextColor.GOLD)
								).append(Component.text(".").color(NamedTextColor.GRAY))
						).clickEvent(
								ClickEvent.suggestCommand("/k show " + kingdom.getName())
						);

				if(viewer instanceof Player viewerPlayer) {
					Kingdom viewerKingdom = Kingdom.fetchKingdom(viewerPlayer);
					if(viewerKingdom == null) return Component.text().append(tag).append(Component.text(" ")).append(renderer.render(source, sourceDisplayName, message, viewer)).build();

					return Component.text().append(tag).append(Component.text(" ")).append(renderer.render(source, sourceDisplayName.color(viewerKingdom.getRelation(kingdom).getPlayerNameColor()), message, viewer)).build();
				}
				return Component.text().append(tag).append(Component.text(" ")).append(renderer.render(source, sourceDisplayName, message, viewer)).build();
			}
		};
	}
}
