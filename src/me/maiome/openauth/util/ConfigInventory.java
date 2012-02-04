package me.maiome.openauth.util;

// bukkit imports
import org.bukkit.configuration.file.YamlConfiguration;

// java imports
import java.util.Map;
import java.util.HashMap;

// internal imports
import me.maiome.openauth.OpenAuth;
import me.maiome.openauth.util.Config;

public enum ConfigInventory {
    MAIN (Config.main),
    DATA (Config.data);

    public final YamlConfiguration config;
    private static final Map<ConfigInventory, YamlConfiguration> store = new HashMap<ConfigInventory, YamlConfiguration>();

    ConfigInventory (final YamlConfiguration config) {
        this.config = config;
    }

    public Configuration getConfig () {
        return this.config;
    }

    public static YamlConfiguration getByConstant (final ConfigInventory ci) {
        return store.get(ci);
    }

    static {
        for (ConfigInventory ci : ConfigInventory.values()) {
            store.put(ci, ci.getConfig());
        }
    }
}