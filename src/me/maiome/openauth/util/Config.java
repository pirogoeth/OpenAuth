package me.maiome.openauth.util;

// java imports
import java.io.File;

// bukkit imports
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;

public class Config {
    // main
    public static OpenAuth plugin;
    public static String version;
    public final static LogHandler log = new LogHandler();
    public final static String plugindir = "plugins/OpenAuth";
    // dynamic
    public static String extension = ".yml";
    public static boolean loaded = false;
    // configuration
    public static YamlConfiguration main;
    public static YamlConfiguration data;
    // files
    public static File mainf;
    public static File dataf;

    // construct
    public Config (OpenAuth instance) {
        this.plugin = instance;
        // create the plugin directory if it does not already exist
        new File(this.plugindir).mkdir();
        // make the plugin data directory
        new File(this.plugindir + File.separator + "/data").mkdir();
        // check for .usetxt for assumed clanforge support
        if (new File(this.plugin + File.separator + ".usetxt").exists() == true) {
            this.log.info("Found .usetxt, changing config extension to .txt");
            this.extension = ".txt";
        }
        // initialise our configurations here
        initialise();
    }

    // (convenience) File instantiator
    private File getFile (String name) {
        return new File(this.plugindir + File.separator + name + this.extension);
    }

    // opens all the files necessary and loads each config
    //   also runs the version checker
    public boolean initialise () {
        if (!loaded) return false;

        this.log.info("Initialising configurations...");
        // create all config file objects
        try {
            mainf = this.getFile("config.yml");
            dataf = this.getFile("data/data.yml");
        } catch (java.lang.Exception e) {
            e.printStackTrace();
            return false;
        }

        // create configuration objects
        main = new YamlConfiguration();
        data = new YamlConfiguration();

        // load the configurations
        try {
            main.load(mainf);
            data.load(dataf);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return false;
        } catch (java.lang.Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void save () {
        try{
            main.save(mainf);
            data.save(dataf);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return;
        } catch (java.lang.Exception e) {
            e.printStackTrace();
            return;
        }
        log.info("Saved configurations.");
        return;
    }

    // TODO - need this.load(), this.checkv()
}