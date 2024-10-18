package net.foxavis.kingdoms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.foxavis.kingdoms.entity.primary.Kingdom;
import net.foxavis.kingdoms.entity.taxes.TaxRate;
import net.foxavis.kingdoms.entity.taxes.TaxRateAdapater;
import net.foxavis.kingdoms.util.CachedDataManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

/**
 * The main class of this plugin, where everything starts up and shuts down
 * @author Kyomi
 */
public final class FoxavisKingdoms extends JavaPlugin {

	private static FoxavisKingdoms instance = null;
	public static FoxavisKingdoms getInstance() { return instance; }
	public static Logger getLoggerInstance() { return instance.getLogger(); }

	public static String getSecretKey() {
		return "Testing"; // Fetch from configuration file
	}

	@Nullable private static Gson gson = null;
	public static @NotNull Gson getGSON() {
		if(gson == null) {
			gson = new GsonBuilder()
					.setPrettyPrinting()
					.serializeNulls()
					.registerTypeAdapter(TaxRate.class, new TaxRateAdapater())
					.create();
		}
		return gson;
	}

//	private List<KingdomChunk> chunks;

	@Override public void onEnable() {
		instance = this;

//		chunks = new ArrayList<>();
//		DynmapCommonAPIListener.register(new DynmapManager());
	}

	@Override public void onDisable() {
		CachedDataManager.shutdownAll();
		Kingdom.saveKingdomIndex();
	}

//	@Override
//	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
//		if(!command.getName().equalsIgnoreCase("claim")) return true;
//
//		if(!(sender instanceof Player player)) return true;
//
//		chunks.add(new KingdomChunk(player.getChunk()));
//
//		DynmapManager.getInstance().getTestingMarker().setCornerLocations(new double[] {}, new double[] {});
//		List<double[]> worldCords = DynmapManager.getPerimeterPoints(chunks);
//		for(int i=0; i<worldCords.size(); i++) {
//			double[] cords = worldCords.get(i);
//			if(cords == null) continue;
//			DynmapManager.getInstance().getTestingMarker().setCornerLocation(i, cords[0], cords[1]);
//		}
//
//		player.sendMessage(Component.text("Added chunk!").color(NamedTextColor.GREEN));
//		return true;
//	}
}