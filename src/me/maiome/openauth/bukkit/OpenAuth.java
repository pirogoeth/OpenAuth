package me.maiome.openauth.bukkit;

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;

// java imports
import java.io.File;
import java.util.logging.Logger;

// core
import me.maiome.openauth.commands.OACommands;
import me.maiome.openauth.util.Permission;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.LogHandler;

// event listeners
// import me.pirogoeth.openauth.event.OAuthPlayerListener;

// bundled imports
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
     * Plugin setup.
     */
    public void onEnable() {
        // initialise permissions manager and config manager
        this.permissionsManager = new Permission(this);
        this.configurationManager = new Config(this);

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

        // register command classes.
        this.commands.registerAndReturn(OACommands.OAParentCommand.class);

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
    public boolean onCommand(CommandSender sender, Command cmd,
            String cmdLabel, String[] args) {
        log.info("onCommand()");
        try {
            log.info("Processing command: " + cmdLabel);
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

    @Override
    public File getFile () {
        return super.getFile();
    }
}