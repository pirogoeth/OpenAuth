package me.maiome.openauth.bukkit;

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.*;
import org.bukkit.configuration.serialization.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.Server;

// ebean
import com.avaje.ebean.EbeanServer;

// java imports
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

// core imports
import me.maiome.openauth.actions.*;
import me.maiome.openauth.commands.*;
import me.maiome.openauth.database.*;
import me.maiome.openauth.event.*;
import me.maiome.openauth.jsonapi.*;
import me.maiome.openauth.metrics.*;
import me.maiome.openauth.mixins.*;
import me.maiome.openauth.security.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.*;

// bundled imports
import com.sk89q.bukkit.util.*; // dynamic command registry
import com.sk89q.util.*; // various handy utilities
import com.sk89q.minecraft.util.commands.*; // command framework

/**
 * This is the entry point for OpenAuth. All the fun starts here.
 *
 * @author pirogoeth
 */
public class OpenAuth extends JavaPlugin {

    /**
     * Static instance holder.
     */
    public static OpenAuth instance;

    /**
     * Logger for everything that might need to be spilled
     * into the console.
     */
    public LogHandler log;

    /**
     * This holds OpenAuth's version.
     */
    public String version;

    /**
     * Holds the hashtag.
     */
    private String hashtag;

    /**
     * Metrics-typed container.
     */
    private static Metrics metrics = null;

    /**
     * Holds our ebean database object.
     */
    private ExtendedDB database;

    /**
     * Manages commands.
     */
    private CommandsManager<CommandSender> commands;

    /**
     * Dynamically registers commands.
     */
    private CommandsManagerRegistration dynamicCommandRegistry;

    /**
     * Lock for the database to prevent persistence errors.
     */
    public static final Object databaseLock = new Object();

    /**
     * Plugin setup.
     */
    public void onEnable() {
        // set our instance
        instance = this;

        // perform config setup.
        new Config(false);

        // set up the log handler
        this.log = new LogHandler();

        // set version number
        this.version = this.getDescription().getVersion();

        // load the build hashtag.
        try {
            Scanner sc = new Scanner(this.getResource("hashtag"));
            this.hashtag = sc.nextLine();
            sc.close();
        } catch (java.lang.Exception e) {
            this.hashtag = "nobuild";
        }

        // initialise the configuration
        Config.getInstance().initialise();

        // search for permissions.
        Permission.search();

        // initialise the database
        this.log.info("NOTE: Initialising database, this *MAY* take a while...");
        this.initialiseDatabase();

        // warn about ebean's rebuild
        if (Config.getConfig().getBoolean("database.advanced.rebuild", false) == true) {
            this.log.info(" - [WARNING] The 'database.rebuild' option in your config.yml is set to true!");
            this.log.info(" - [WARNING] This means that your database will be recreated every time the server starts and your data will be lost!");
            this.log.info(" - [WARNING] If this is not what you want, stop the server and change the entry to false.");
        }

        // set logging level
        this.log.setDebugging((Config.getConfig().getBoolean("debug", false) == false) ? false : true);

        // run database updates
        DatabaseUpdater.runUpdates();

        // initialise the OAServer
        new OAServer();
        // initialise out session controller
        new SessionController();

        // initialise the mixin manager, which instantiates the generic class loader.
        new MixinManager();

        // check if we need to override.
        if (Config.getConfig().getBoolean("override", false) == true) {
            if (this.getServer().hasWhitelist() == true) { // override the whitelisting in Bukkit for mine?
                this.getServer().setWhitelist(false);
                this.log.info(" - Bukkit whitelisting is now OFF!");
            }
        }

        // start scheduler tasks
        OAServer.getInstance().startSchedulerTasks();
        SessionController.getInstance().startSchedulerTasks();

        // register our command manager.
        this.commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender sender, String perm) {
                if (sender instanceof org.bukkit.command.ConsoleCommandSender) {
                    return true;
                }
                return Permission.has((Player) sender, perm);
            }
        };

        // initialise the command registration
        this.dynamicCommandRegistry = new CommandsManagerRegistration(this, this.commands);

        // setup instance injector
        this.commands.setInjector(new SimpleInjector(this));

        // register listener(s)
        this.registerEvents(new OAListener());
        this.registerEvents(new OAExplosionListener());

        // register base command class.
        this.dynamicCommandRegistry.register(OACommands.OAParentCommand.class);
        this.dynamicCommandRegistry.register(OARootAliasCommands.LoginRootAliasCommand.class);
        this.dynamicCommandRegistry.register(OARootAliasCommands.LogoutRootAliasCommand.class);
        this.dynamicCommandRegistry.register(OARootAliasCommands.RegisterRootAliasCommand.class);

        // generate sessions for all users (in the case of a reload...)
        SessionController.getInstance().createAll();

        // setup PluginMetrics
        if (Config.getConfig().getBoolean("metrics-enabled", true) == true) {
            if (Config.getConfig().getBoolean("show-metrics-notice", true) == true) {
                String[] metrics_warning = {
                    " - NOTICE: You have chosen to OPT-IN to PluginMetrics for this plugin!",
                    " -   PluginMetrics will anonymously collect statistical data about the server and this plugin to send back to the plugin author.",
                    " -   The data collected will only be used for statistic gathering to keep track of certain aspects of the plugin and its development.",
                    " -   If you'd prefer to disable PluginMetrics and keep it from loading in this plugin, open the config.yml for this plugin and change metrics-enabled to false and restart your server."
                };
                for (String line : metrics_warning) {
                    this.log.info(line);
                }
            }
            try {
                this.metrics = new Metrics(this);
            } catch (java.lang.Exception e) {
                this.log.warning("Could not load PluginMetrics!");
                e.printStackTrace();
            }
        }

        // initialise the Actions enum.
        Actions.init();

        // initialise the JSONAPI call handler
        try {
            new OAJSONAPICallHandler();
            OAJSONAPINativeMethods.load();
            this.metrics.addCustomData(OAJSONAPICallHandler.getInstance().tracker); // add metrics data tracker
        } catch (java.lang.NoClassDefFoundError e) {
            this.log.warning("JSONAPI call handler could not be loaded -- is JSONAPI loaded?");
        } catch (java.lang.Exception e) {
            this.log.warning("An exception was caught while loading the JSONAPI call handler -- is JSONAPI loaded?");
        }

        // enable metrics.
        try {
            this.metrics.enable();
        } catch (java.lang.Exception e) {
            this.log.warning("Could not load PluginMetrics!");
            e.printStackTrace();
        }

        // make the mixin manager load all mixins.
        MixinManager.getInstance().load();

        // loaded.
        this.log.info("Enabled OpenAuth [" + this.version + "-" + this.hashtag + "].");
    };

    /**
     * Deal with the disabling of the plugin.
     */
    @Override
    public void onDisable() {
        // set each player offline before shutting down
        for (OAPlayer player : OAPlayer.players.values()) {
            player.setOffline();
        }
        // unload mixins
        MixinManager.getInstance().unload();
        // save the whitelist
        OAServer.getInstance().getWhitelistHandler().saveWhitelist();
        // shutdown all OA tasks
        OAServer.getInstance().cancelAllOATasks();
        // destroy all living sessions (in the case of a reload)
        SessionController.getInstance().destroyAll();

        this.log.info("Disabled OpenAuth" + this.version + "-" + this.hashtag + ".");
    }

    /**
     * Called to process a command.
     */
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd,
            String cmdLabel, String[] args) {
        try {
            commands.execute(cmd.getName(), args, sender, sender);
        } catch (CommandPermissionsException e) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(ChatColor.RED + "Number expected, string received.");
            } else {
                sender.sendMessage(ChatColor.RED + "An error has occurred. See console.");
                e.printStackTrace();
            }
        } catch (com.sk89q.minecraft.util.commands.CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }

        return true;
    }

    // database setup
    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(DBBanRecord.class);
        list.add(DBPlayer.class);
        list.add(DBWhitelist.class);
        list.add(DBWorldRecord.class);
        return list;
    };

    private void initialiseDatabase() {
        Configuration config = Config.getConfig();

        this.database = new ExtendedDB(OpenAuth.getInstance());

        this.database.initializeDatabase(
            config.getString("database.driver", "org.sqlite.JDBC"),
            config.getString("database.url", "jdbc:sqlite:{DIR}/{NAME}.db"),
            config.getString("database.username", "captain"),
            config.getString("database.password", "narwhal"),
            config.getString("database.isolation", "SERIALIZABLE"),
            config.getBoolean("database.advanced.logging", true),
            config.getBoolean("database.advanced.rebuild", false)
        );

        this.getDatabase().createSqlQuery("PRAGMA journal_mode=WAL;");
    }

    // various support methods

    /**
     * Looks for a command inside of a string array.
     */
    public String[] detectCommands(String[] split) {
        split[0] = split[0].substring(1);

        String search = split[0].toLowerCase();

        // detect the command.
        if (this.commands.hasCommand(search)) {
        } else if (split[0].length() >= 2 && split[0].charAt(0) == '/'
                   && this.commands.hasCommand(search.substring(1))) {
            split[0] = split[0].substring(1);
        }

        return split;
    }

    /**
     * Allows us to publically hand out our File instance (probably not good).
     */
    @Override
    public File getFile() {
        return super.getFile();
    }

    /**
     * I think the other version of this was causing issues, so here's this!
     */
    public File getFolder() {
        return new File("plugins" + File.separator + "OpenAuth");
    }

    /**
     * Returns the hashtag.
     */
    public String getHashtag() {
        return this.hashtag;
    }

    /**
     * Overrides the getDatabase() in JavaPlugin.class so we can use ExtendedDB.
     */
    @Override
    public EbeanServer getDatabase() {
        return this.database.getDatabase();
    }

    /**
     * Returns the Metrics instance.
     */
    public static Metrics getMetrics() {
        return metrics;
    }

    /**
     * Returns myself (should be able to cast down to JavaPlugin...
     */
    public static OpenAuth getInstance() {
        return instance;
    }

    /**
     * Casts +instance+ down to a JavaPlugin and returns it.
     */
    public static JavaPlugin getJavaPlugin() {
        return (JavaPlugin) instance;
    }

    /**
     * Returns the CommandsManager instance used by OpenAuth.
     */
    public CommandsManager<CommandSender> getCommandsManager() {
        return this.commands;
    }

    /**
     * Returns the CommandsManagerRegistration instance used by OpenAuth.
     */
    public CommandsManagerRegistration getCommandsManagerRegistration() {
        return this.dynamicCommandRegistry;
    }

    /**
     * DEPRECATED: Shorthand to register an event listener.
     */
    @Deprecated
    public void registerEvents(Listener listener) {
        OAServer.getInstance().registerEvents(listener);
    }

    /**
     * Adds a custom data tracker to metrics.
     */
    public void addCustomMetricsTracker(Tracker tracker) {
        this.metrics.addCustomData(tracker);
    }

    /**
     * Register parent commands with OpenAuth's instance of the WorldEdit command framework.
     */
    public void registerCommandClass(Class<?> clazz) {
        this.dynamicCommandRegistry.register(clazz);
    }
}