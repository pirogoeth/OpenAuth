package me.maiome.openauth.bukkit;

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
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

// core
import me.maiome.openauth.commands.*;
import me.maiome.openauth.event.*;
import me.maiome.openauth.util.Permission;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.LogHandler;

// event listeners
// import me.pirogoeth.openauth.event.OAuthPlayerListener;

// bundled imports
import com.sk89q.bukkit.util.CommandRegistration; // dynamic command registry
import com.sk89q.util.*;
import com.sk89q.minecraft.util.commands.*; // command framework

/**
 * This is the entry point for OpenAuth. All the fun starts here.
 *
 * @author pirogoeth
 */
public class OpenAuth extends JavaPlugin {
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
    public OAServer oaserver = new OAServer(this, this.getServer());

    /**
     * Holds OAPlayer instances.
     */
    public Map<String, OAPlayer> players = new HashMap<String, OAPlayer>();

    /**
     * Holds the gateway to all permission verification.
     */
    private Permission permissionsManager;

    /**
     * Initialises configurations and writes defaults.
     */
    private Config configurationManager;

    /**
     * Manages commands.
     */
    private CommandsManager<CommandSender> commands;

    /**
     * Dynamically registers commands.
     */
    private CommandRegistration dynamicCommandRegistry;

    /**
     * Plugin setup.
     */
    public void onEnable() {
        // initialise permissions manager and config manager as well as dynamic command registration
        this.permissionsManager = new Permission(this);
        this.configurationManager = new Config(this);
        this.dynamicCommandRegistry = new CommandRegistration(this);

        // set version number
        this.version = this.getDescription().getVersion();

        // register our command manager.
        this.commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender player, String perm) {
                return Permission.has((Player) player, perm);
            }
        };

        // setup instance injector
        this.commands.setInjector(new SimpleInjector(this));

        // register listener(s)
        this.registerEvents(new OAListener(this));

        // register command classes.
        this.registerCommands(this.commands.registerAndReturn(OACommands.OAParentCommand.class));

        // loaded.
        log.info("Enabled version " + version + ".");
    };

    /**
     * Deal with the disabling of the plugin.
     */
    @Override
    public void onDisable () {
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

    @Override
    public File getFile () {
        return super.getFile();
    }

    /**
     * Register commands with the magical dynamic handler.
     */
    protected void registerCommands(List<com.sk89q.minecraft.util.commands.Command> commands) {
        this.dynamicCommandRegistry.registerAll(commands);
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
     * Wraps a player into an OAPlayer instance.
     */
    public OAPlayer wrapOAPlayer(Player player) {
        if(!(players.containsKey(player.getName()))) {
            OAPlayer _player = new OAPlayer(this.oaserver, player);
            players.put(player.getName(), _player);
            return _player;
        } else if (players.containsKey(player.getName())) {
            return players.get(player.getName());
        }
        return null;
    }

    public OAPlayer wrapOAPlayer(String _player) {
        Player player = this.getServer().getPlayer(_player);
        if (!(player isinstance org.bukkit.entity.LivingEntity) && (player isinstance org.bukkit.entity.Player)) return null;
        if(!(players.containsKey(player.getName()))) {
            OAPlayer _player = new OAPlayer(this.oaserver, player);
            players.put(player.getName(), _player);
            return _player;
        } else if (players.containsKey(player.getName())) {
            return players.get(player.getName());
        }
        return null;
    }
}
