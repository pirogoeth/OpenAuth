package me.maiome.openauth.util;

// java imports
import java.io.File;
import java.io.InputStream;

// bukkit imports
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.util.*;

public class Config {

    private static final LogHandler log = new LogHandler();
    private static Config instance = null;

    public static final String plugindir = "plugins" + File.separator + "OpenAuth";
    public static String version;
    public static String extension = ".yml";
    public static boolean loaded = false;

    protected static YamlConfiguration main = null;
    protected static File mainf = null;

    public static Config getInstance() {
        return instance;
    }

    public static YamlConfiguration getConfig() {
        return main;
    }

    // construct
    public Config(boolean initialise) {
        // create the plugin directory if it does not already exist
        new File(this.plugindir).mkdir();
        // check for .usetxt for assumed clanforge support
        if (new File(this.plugindir + File.separator + ".usetxt").exists() == true) {
            log.info("Found .usetxt, changing config extension to .txt");
            this.extension = ".txt";
        }
        // initialise our configurations here
        if (initialise) this.initialise();
    }

    // (convenience) File instantiator
    private File getFile(String name) {
        return new File(this.plugindir + File.separator + name + this.extension);
    }

    // opens all the files necessary and loads each config
    //   also runs the version checker
    public void initialise() {
        if (loaded) return;

        log.info("Initialising configuration...");
        // create all config file objects
        try {
            mainf = this.getFile("config");
        } catch (java.lang.Exception e) {}

        // create configuration objects
        main = new YamlConfiguration();

        // load the configurations
        try {
            main.load(mainf);
            main.addDefaults(YamlConfiguration.loadConfiguration(OpenAuth.getInstance().getResource("config.yml")));
            main.options().copyDefaults(true);
            main.save(mainf);
        } catch (java.io.FileNotFoundException e) {
            log.info("Configuration files do not exist, creating them.");
            try {
                OpenAuth.getInstance().saveResource("config.yml", true);
                initialise();
                return;
            } catch (java.lang.Exception ex) {
                log.info("Could not create new configuration files:");
                ex.printStackTrace();
            }
        } catch (java.lang.Exception e) {
            e.printStackTrace();
            return;
        }
        loaded = true;
        return;
    }

    public static void save() {
        try {
            main.save(mainf);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return;
        } catch (java.lang.Exception e) {
            e.printStackTrace();
            return;
        }
        log.debug("Saved configurations.");
        return;
    }
}