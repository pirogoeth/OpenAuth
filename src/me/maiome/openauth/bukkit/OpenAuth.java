package me.maiome.openauth.bukkit;

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.Server;

// java imports
import java.io.File;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// core imports
import me.maiome.openauth.actions.*;
import me.maiome.openauth.commands.*;
import me.maiome.openauth.event.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.Permission;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.ConfigInventory;
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
     * Holds an OAServer instance.
     */
    private static OAServer oaserver;

    /**
     * Holds OAPlayer instances.
     */
    public Map<String, OAPlayer> players = new HashMap<String, OAPlayer>();

    /**
     * Session controller.
     */
    private static SessionController sc;

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
     * Plugin setup.
     */
    public void onEnable() {
        // initialise the configuration
        this.configurationManager.initialise();

        // set logging level
        log.setExtraneousDebugging((ConfigInventory.MAIN.getConfig().getBoolean("debug", false) == false) ? false : true);

        // initialise the OAServer
        new OAServer(this, this.getServer());
        // initialise out session controller
        new SessionController(this);

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

        // register command classes.
        this.dynamicCommandRegistry.register(OACommands.OAParentCommand.class);
        this.dynamicCommandRegistry.register(OAPointsCommand.OAPointsParentCommand.class);

        // generate sessions for all users
        sc.createAll();

        // loaded.
        log.info("Enabled version " + version + ".");
    };

    /**
     * Deal with the disabling of the plugin.
     */
    @Override
    public void onDisable () {
        // save ALL the bans!
        oaserver.saveBans(false);
        // save the whitelist
        oaserver.getWhitelistHandler().saveWhitelist();
        // shutdown all OA tasks
        oaserver.cancelAllOATasks();
        // destroy all living sessions in case this is a reload
        sc.destroyAll();
        // save the data.
        Config.save_data();

        log.info("Disabled version " + version + ".");
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
    public File getFile () {
        return super.getFile();
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
     * Returns the CommandsManager instance used by OpenAuth.
     */
    public CommandsManager<CommandSender> getCommandsManager() {
        return this.commands;
    }

    /**
     * Shorthand to register an event listener.
     */
    private void registerEvents(Listener listener) {
        this.getServer().getPluginManager().registerEvents(
            listener,
            (Plugin) this);
        return;
    }

    /**
     * Register commands with OpenAuth's instance of the WorldEdit command framework.
     */
    public void registerCommandClass(Class<?> clazz) {
        this.dynamicCommandRegistry.register(clazz);
    }

    /**
     * Whether or not a player can be easily wrapped.
     */
    public boolean wrappable(String name) {
        return (players.containsKey(name) && (this.getServer().getPlayer(name) != null));
    }

    public boolean wrappable(Player player) {
        return this.wrappable(player.getName());
    }

    /**
     * Wraps a player into an OAPlayer instance.
     */
    public OAPlayer wrap(Player player) {
        if (!(player instanceof org.bukkit.entity.LivingEntity) && (player instanceof org.bukkit.entity.Player)) return null;
        if (!(players.containsKey(player.getName()))) {
            OAPlayer _player = new OAPlayer(player);
            players.put(player.getName(), _player);
            return _player;
        } else if (players.containsKey(player.getName())) {
            return players.get(player.getName());
        }
        return null;
    }

    public OAPlayer wrap(PlayerLoginEvent event) {
        if (!(players.containsKey(event.getPlayer().getName()))) {
            OAPlayer _player = new OAPlayer(event);
            players.put(event.getPlayer().getName(), _player);
            return _player;
        } else if (players.containsKey(event.getPlayer().getName())) {
            return players.get(event.getPlayer().getName());
        }
        return null;
    }

    public OAPlayer wrap(String _player) {
        Player player = this.getServer().getPlayer(_player);
        return this.wrap(player);
    }

    public OAPlayer forciblyWrapOAPlayer(String _player) {
        if (!(players.containsKey(_player))) {
            return null;
        } else {
            return players.get(_player);
        }
    }
}
