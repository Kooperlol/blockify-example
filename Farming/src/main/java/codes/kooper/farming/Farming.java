package codes.kooper.farming;

import org.bukkit.plugin.java.JavaPlugin;

public final class Farming extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new FarmListener(), this);
    }

    public static Farming getInstance() {
        return getPlugin(Farming.class);
    }

}
