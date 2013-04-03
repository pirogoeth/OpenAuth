package me.maiome.openauth.util;

// bukkit imports
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

// java imports
import java.io.File;
import java.util.Map;
import java.util.HashMap;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.util.Config;

@Deprecated
public enum ConfigInventory {

    MAIN(Config.main, Config.mainf);

    public final YamlConfiguration config;
    public final File file;
    private static final Map<ConfigInventory, Configuration> store = new HashMap<ConfigInventory, Configuration>();

    ConfigInventory(final YamlConfiguration config, final File file) {
        this.config = config;
        this.file = file;
    }

    public Configuration getConfig() {
        return this.config;
    }

    public File getFile() {
        return this.file;
    }

    public synchronized void save() {
        try {
            this.config.save(this.file);
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
        return;
    }

    public void reload(boolean save) {
        if (save == true) {
            this.save();
        }
        YamlConfiguration _conf = YamlConfiguration.loadConfiguration(new File(this.file.getPath()));
        store.put(this, _conf);
    }

    public static YamlConfiguration getByConstant(final ConfigInventory ci) {
        return (YamlConfiguration) store.get(ci);
    }

    static {
        for (ConfigInventory ci : ConfigInventory.values()) {
            store.put(ci, ci.getConfig());
        }
    }
}