package me.maiome.openauth.util;

// java imports
import java.io.File;

// internal imports
import me.maiome.openauth.OpenAuth;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;

// yaml util imports
import com.sk89q.util.YAMLFormat;
import com.sk89q.util.YAMLProcessor;

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
    public static YAMLProcessor main;
    public static YAMLProcessor data;
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
        this.log.info("Initialising configurations...");
        // load all config files
        try {
            mainf = this.getFile("config.yml");
            dataf = this.getFile("data/data.yml");
        } catch (java.lang.Exception e) {
            return false;
        }

        // create configuration objects
        main = new YAMLProcessor(mainf, true, YAMLFormat.EXTENDED);
        data = new YAMLProcessor(dataf, true, YAMLFormat.EXTENDED);

        // load the configurations
        try {
            main.load();
            data.load();
        } catch (java.lang.Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // TODO - need this.load(), this.checkv()
}