package net.foxavis.kingdoms.listeners;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.objects.KingdomPlayer;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

	private final FoxavisKingdoms plugin;
	public PlayerListener(FoxavisKingdoms plugin) {
		this.plugin = plugin;
	}

	@EventHandler public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if(KingdomPlayer.fetchPlayer(player) == null) new KingdomPlayer(player);

		String kingdomId = player.getPersistentDataContainer().get(Kingdom.getPDCKingdomKey(), PersistentDataType.STRING);
		if(kingdomId != null) {
			Kingdom kingdom = Kingdom.fetchKingdom(UUID.fromString(kingdomId));
			if(kingdom == null) {
				player.getPersistentDataContainer().remove(Kingdom.getPDCKingdomKey());
				player.sendMessage(Component.text("The leader of your kingdom disbanded said kingdom. You are on your own now...").color(NamedTextColor.YELLOW));
				player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1, 0);
			} else if(!kingdom.isMember(player)) {
				player.getPersistentDataContainer().remove(Kingdom.getPDCKingdomKey());
				player.sendMessage(Component.text("You were kicked from your kingdom. You are on your own now...").color(NamedTextColor.RED));
				player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1, 0);
			}
		}
	}

	private Map<UUID, UUID> lastEntered = new HashMap<>();
	@EventHandler public void onMove(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		if(lastEntered == null)
			lastEntered = new HashMap<>();

		Chunk chunk = player.getLocation().getChunk();
		Kingdom kingdom = Kingdom.fetchKingdom(chunk);
		UUID last = lastEntered.getOrDefault(player.getUniqueId(), null);

		if(kingdom == null) {
			if(last != null) {
				Kingdom lastK = Kingdom.fetchKingdom(last);
				if(lastK == null) {
					lastEntered.put(player.getUniqueId(), null);
					return;
				}

				player.sendActionBar(Component.text("Leaving: ").color(NamedTextColor.RED).append(lastK.getTag()).appendSpace().append(Component.text(lastK.getName()).color(NamedTextColor.YELLOW)));
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 0.5f, 1.5f);
			}

			lastEntered.put(player.getUniqueId(), null);
			return;
		}

		if((last == null) || !last.equals(kingdom.getKingdomId())) {
			player.sendActionBar(Component.text("Entering: ").color(NamedTextColor.GREEN).append(kingdom.getTag()).appendSpace().append(Component.text(kingdom.getName()).color(NamedTextColor.YELLOW)));
			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 0.5f, 0.5f);
			lastEntered.put(player.getUniqueId(), kingdom.getKingdomId());
		}
	}
}
