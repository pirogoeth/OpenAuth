package me.maiome.openauth.util;

// java imports
import java.io.File;
import java.io.InputStream;

// bukkit imports
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;

public class Config {

    // main
    private static OpenAuth controller;
    public static String version;
    private final static LogHandler log = new LogHandler();
    public final static String plugindir = "plugins" + File.separator + "OpenAuth";
    // dynamic
    public static String extension = ".yml";
    public static boolean loaded = false;
    // configuration
    public static YamlConfiguration main;
    // files
    public static File mainf;

    // construct
    public Config(OpenAuth instance, boolean initialise) {
        this.controller = instance;
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
            main.addDefaults(YamlConfiguration.loadConfiguration(this.controller.getResource("config.yml")));
            main.options().copyDefaults(true);
            main.save(mainf);
        } catch (java.io.FileNotFoundException e) {
            log.info("Configuration files do not exist, creating them.");
            try {
                this.controller.saveResource("config.yml", true);
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
        log.exDebug("Saved configurations.");
        return;
    }
}