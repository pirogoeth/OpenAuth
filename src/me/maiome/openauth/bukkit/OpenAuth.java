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
import java.io.File;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import me.maiome.openauth.util.Permission;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LocationSerialisable;
import me.maiome.openauth.util.LogHandler;

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

    static {
        ConfigurationSerialization.registerClass(LocationSerialisable.class);
    }

    /**
     * Static instance holder.
     */
    public static JavaPlugin plugin;

    /**
     * Initialises configurations and writes defaults.
     */
    private Config configurationManager = new Config(this, false);

    /**
     * Logger for everything that might need to be spilled
     * into the console.
     */
    public static final LogHandler log = new LogHandler();

    /**
     * This holds OpenAuth's version.
     */
    public String version;

    /**
     * Metrics-typed container.
     */
    private static Metrics metrics = null;

    /**
     * Mixin Manager.
     */
    private static MixinManager mixinManager;

    /**
     * Holds an OAServer instance.
     */
    private static OAServer oaserver;

    /**
     * Holds our ebean database object.
     */
    private ExtendedDB database;

    /**
     * Session controller.
     */
    private static SessionController sc;

    /**
     * JSONAPI Call handler.
     */
    private static OAJSONAPICallHandler jsch = null;

    /**
     * Holds the gateway to all permission verification.
     */
    private Permission permissionsManager;

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
        OpenAuth.setInstance(this);

        // load the build hashtag.
        String hashtag = (YamlConfiguration.loadConfiguration(this.getResource("plugin.yml"))).getString("hashtag", "nobuild");

        // initialise the configuration
        this.configurationManager.initialise();

        // initialise the database
        log.info("NOTE: Initialising database, this *MAY* take a while...");
        this.initialiseDatabase();

        // set logging level
        log.setExtraneousDebugging((ConfigInventory.MAIN.getConfig().getBoolean("debug", false) == false) ? false : true);

        // initialise the OAServer
        new OAServer(this, this.getServer());
        // initialise out session controller
        new SessionController(this);

        // initialise the mixin manager, which instantiates the generic class loader.
        this.mixinManager = new MixinManager();

        // check if we need to override.
        if (ConfigInventory.MAIN.getConfig().getBoolean("override", false) == true) {
            if (this.getServer().hasWhitelist() == true) { // override the whitelisting in Bukkit for mine?
                this.getServer().setWhitelist(false);
                log.info(" => Bukkit whitelisting is now OFF!");
            }
        }

        // initialise permissions manager and config manager as well as dynamic command registration
        this.permissionsManager = new Permission(this);

        // set version number
        this.version = this.getDescription().getVersion();

        // start scheduler tasks
        oaserver.startSchedulerTasks();
        sc.startSchedulerTasks();

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

        // load ALL the bans!
        oaserver.loadBans();

        // setup instance injector
        this.commands.setInjector(new SimpleInjector(this));

        // register listener(s)
        this.registerEvents(new OAListener(this));
        this.registerEvents(new OAExplosionListener(this));

        // register base command class.
        this.dynamicCommandRegistry.register(OACommands.OAParentCommand.class);

        // generate sessions for all users
        sc.createAll();

        // setup PluginMetrics
        if (ConfigInventory.MAIN.getConfig().getBoolean("metrics-enabled", true) == true) {
            if (ConfigInventory.MAIN.getConfig().getBoolean("show-metrics-notice", true) == true) {
                String[] metrics_warning = {
                    "NOTICE: You have chosen to OPT-IN to PluginMetrics for this plugin!",
                    "PluginMetrics will anonymously collect statistical data about the server and this plugin to send back to the plugin author.",
                    "The data collected will only be used for statistic gathering to keep track of certain aspects of the plugin and its development.",
                    "If you'd prefer to disable PluginMetrics and keep it from loading in this plugin, open the config.yml for this plugin and change metrics-enabled to false and reload your server.",
                    "Or, while the server is running, you can run /oa settings metrics-enabled false while logged in or on the console to disable statistics collection."
                };
                for (String line : metrics_warning) {
                    log.info(line);
                }
            }
            try {
                this.metrics = new Metrics(this);
                Actions.loadMetricsData();
            } catch (java.lang.Exception e) {
                log.warning("Could not load PluginMetrics!");
                e.printStackTrace();
            }
        }

        // initialise the JSONAPI call handler
        try {
            new OAJSONAPICallHandler(this);
            OAJSONAPINativeMethods.load();
            this.metrics.addCustomData(OpenAuth.getJSONAPICallHandler().tracker); // add metrics data tracker
        } catch (java.lang.NoClassDefFoundError e) {
            log.warning("JSONAPI call handler could not be loaded -- is JSONAPI loaded?");
        } catch (java.lang.Exception e) {
            log.warning("An exception was caught while loading the JSONAPI call handler -- is JSONAPI loaded?");
        }

        // enable metrics.
        try {
            this.metrics.enable();
        } catch (java.lang.Exception e) {
            log.warning("Could not load PluginMetrics!");
            e.printStackTrace();
        }

        // make the mixin manager load all mixins.
        this.mixinManager.load();

        // loaded.
        log.info("Enabled version [" + version + "b" + hashtag + "].");
    };

    /**
     * Deal with the disabling of the plugin.
     */
    @Override
    public void onDisable() {
        // set each player offline before shutting down
        for (Map.Entry<String, OAPlayer> entry : OAPlayer.players.entrySet()) {
            ((OAPlayer) entry.getValue()).setOffline();
        }
        // save ALL the bans!
        oaserver.saveBans(false);
        // save the whitelist
        oaserver.getWhitelistHandler().saveWhitelist();
        // shutdown all OA tasks
        oaserver.cancelAllOATasks();
        // destroy all living sessions in case this is a reload
        sc.destroyAll();
        // save the data.
        ConfigInventory.DATA.save();

        log.info("Disabled v" + version + ".");
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
        list.add(DBPlayer.class);
        list.add(DBWhitelist.class);
        list.add(DBChatChannel.class);
        // list.add(DBBanlist.class);
        return list;
    };

    private void initialiseDatabase() {
        Configuration config = ConfigInventory.MAIN.getConfig();

        this.database = new ExtendedDB(OpenAuth.getInstance());

        this.database.initializeDatabase(
            config.getString("database.driver", "org.sqlite.JDBC"),
            config.getString("database.url", "jdbc:sqlite:{DIR}/{NAME}.db"),
            config.getString("database.username", "captain"),
            config.getString("database.password", "narwhal"),
            config.getString("database.isolation", "SERIALIZABLE"),
            config.getBoolean("database.logging", false),
            config.getBoolean("database.rebuild", true)
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
     * Allows us to publically hand out our File instance.
     */
    @Override
    public File getFile() {
        return super.getFile();
    }

    /**
     * I think the other version of this was causing issues, so here's this!
     */
    @Override
    public File getDataFolder() {
        return new File("plugins" + File.separator + "OpenAuth");
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
     * Returns the server instance that we are using.
     */
    public static OAServer getOAServer() {
        return oaserver;
    }

    /**
     * Sets the server instance that we will be using.
     *
     * This *CANNOT* be used if the server is already set.
     */
    public static void setOAServer(OAServer oaserver) {
        if (OpenAuth.oaserver != null) {
            throw new UnsupportedOperationException("Cannot redefine a OAServer instance.");
        }

        OpenAuth.oaserver = oaserver;
    }

    /**
     * Returns the JavaPlugin instance that we are using.
     */
    public static JavaPlugin getInstance() {
        return plugin;
    }

    /**
     * Sets the javaPlugin instance that we will be using.
     *
     * This *CANNOT* be used if the plugin is already set.
     */
    public static void setInstance(JavaPlugin plugin) {
        if (OpenAuth.plugin != null) {
            throw new UnsupportedOperationException("Cannot redefine a JavaPlugin instance.");
        }

        OpenAuth.plugin = plugin;
    }

    /**
     * Returns the session controller instance.
     */
    public static SessionController getSessionController() {
        return sc;
    }

    /**
     * Sets the SessionController instance that we will be using.
     *
     * This *CANNOT* be reset if the controller is already set.
     */
    public static void setSessionController(SessionController sc) {
        if (OpenAuth.sc != null) {
            throw new UnsupportedOperationException("Cannot redefine a SessionController instance.");
        }

        OpenAuth.sc = sc;
    }

    /**
     * Returns the JSONAPI call handler instance.
     */
    public static OAJSONAPICallHandler getJSONAPICallHandler() {
        return jsch;
    }

    /**
     * Sets the OAJSONAPICallHandler instance that we will be using.
     *
     * This *CANNOT* be reset if the handler is set and not null.
     */
    public static void setJSONAPICallHandler(OAJSONAPICallHandler jsch) {
        if (OpenAuth.jsch != null) {
            throw new UnsupportedOperationException("Cannot redefine a OAJSONAPICallHandler instance.");
        }

        OpenAuth.jsch = jsch;
    }


    /**
     * Returns the active password security validator.
     */
    public static IPasswordSecurity getActivePWSecurityValidator() {
        return OAPasswordSecurity.getActiveSecurityValidator();
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
     * Shorthand to register an event listener.
     */
    public void registerEvents(Listener listener) {
        this.getServer().getPluginManager().registerEvents(
            listener,
            (Plugin) this);
        return;
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

    /**
     * Whether or not a player can be easily wrapped.
     */
    public boolean wrappable(Object obj) {
        log.warning("DEPRECATED: SOMETHING USED OpenAuth.wrappable()!");
        return OAPlayer.hasPlayer(obj);
    }

    /**
     * Wraps a player into an OAPlayer instance.
     */
    public OAPlayer wrap(Object obj) {
        log.warning("DEPRECATED: SOMETHING USED OpenAuth.wrap()!");
        return OAPlayer.getPlayer(obj);
    }
}