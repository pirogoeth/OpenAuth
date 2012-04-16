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
    public OAServer oaserver;

    /**
     * Holds OAPlayer instances.
     */
    public Map<String, OAPlayer> players = new HashMap<String, OAPlayer>();

    /**
     * Session controller.
     */
    public SessionController sc;

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

        // initialise our OAServer instance
        this.oaserver = new OAServer(this, this.getServer());
        // initialise out session controller
        this.sc = new SessionController(this);

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
        this.oaserver.startSchedulerTasks();
        this.sc.startSchedulerTasks();

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
        this.oaserver.loadBans();

        // setup instance injector
        this.commands.setInjector(new SimpleInjector(this));

        // register listener(s)
        this.registerEvents(new OAListener(this));
        this.registerEvents(new OAExplosionListener(this));

        // register command classes.
        this.dynamicCommandRegistry.register(OACommands.OAParentCommand.class);

        // generate sessions for all users
        this.sc.createAll();

        // loaded.
        log.info("Enabled version " + version + ".");
    };

    /**
     * Deal with the disabling of the plugin.
     */
    @Override
    public void onDisable () {
        // save ALL the bans!
        this.oaserver.saveBans();

        // save the data as our last step.
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
    public OAServer getOAServer() {
        return this.oaserver;
    }

    /**
     * Returns the session controller instance.
     */
    public SessionController getSessionController() {
        return this.sc;
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
     * Wraps a player into an OAPlayer instance.
     */
    public OAPlayer wrapOAPlayer(Player player) {
        if (!(player instanceof org.bukkit.entity.LivingEntity) && (player instanceof org.bukkit.entity.Player)) return null;
        if (!(players.containsKey(player.getName()))) {
            OAPlayer _player = new OAPlayer(this.oaserver, player);
            players.put(player.getName(), _player);
            return _player;
        } else if (players.containsKey(player.getName())) {
            return players.get(player.getName());
        }
        return null;
    }

    public OAPlayer wrapOAPlayer(PlayerLoginEvent event) {
        if (!(players.containsKey(event.getPlayer().getName()))) {
            OAPlayer _player = new OAPlayer(this.oaserver, event);
            players.put(event.getPlayer().getName(), _player);
            return _player;
        } else if (players.containsKey(event.getPlayer().getName())) {
            return players.get(event.getPlayer().getName());
        }
        return null;
    }

    public OAPlayer wrapOAPlayer(String _player) {
        Player player = this.getServer().getPlayer(_player);
        if (!(player instanceof org.bukkit.entity.LivingEntity) && (player instanceof org.bukkit.entity.Player) ||
            (player instanceof org.bukkit.OfflinePlayer)) return null;
        if (!(players.containsKey(_player))) {
            OAPlayer __player = new OAPlayer(this.oaserver, player);
            players.put(player.getName(), __player);
            return __player;
        } else if (players.containsKey(player.getName())) {
            return players.get(player.getName());
        }
        return null;
    }

    public OAPlayer forciblyWrapOAPlayer(String _player) {
        if (!(players.containsKey(_player))) {
            return null;
        } else {
            return players.get(_player);
        }
    }
}
