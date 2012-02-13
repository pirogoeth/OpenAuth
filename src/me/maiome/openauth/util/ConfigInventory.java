package me.maiome.openauth.util;

// yaml util imports
import com.sk89q.util.YAMLProcessor;

// java imports
import java.util.Map;
import java.util.HashMap;

// internal imports
import me.maiome.openauth.OpenAuth;
import me.maiome.openauth.util.Config;

public enum ConfigInventory {
    MAIN (Config.main),
    DATA (Config.data);

    public final YAMLProcessor config;
    private static final Map<ConfigInventory, YAMLProcessor> store = new HashMap<ConfigInventory, YAMLProcessor>();

    ConfigInventory (final YAMLProcessor config) {
        this.config = config;
    }

    public Configuration getConfig () {
        return this.config;
    }

    public static YAMLProcessor getByConstant (final ConfigInventory ci) {
        return store.get(ci);
    }

    static {
        for (ConfigInventory ci : ConfigInventory.values()) {
            store.put(ci, ci.getConfig());
        }
    }
}