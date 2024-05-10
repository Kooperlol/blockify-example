package codes.kooper.mining;

import org.bukkit.plugin.java.JavaPlugin;

public final class Mining extends JavaPlugin {

    @Override
    public void onEnable() {

    }

    public static Mining getInstance() {
        return getPlugin(Mining.class);
    }
}
