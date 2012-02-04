package me.maiome.openauth.util;

// Bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
// Java imports
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
// Core imports
import me.maiome.openauth.OpenAuth;
import me.maiome.openauth.Util.Permission;
import me.maiome.openauth.Util.Registry;
import me.maiome.openauth.Util.CommandException;
import me.maiome.openauth.Util.RegistryException;
import me.maiome.openauth.util.LogHandler;

public class Command {
    public LogHandler log = new LogHandler();
    public Config configuration;
    public Permission permissions;
    public Registry registry;
    public OpenAuth plugin;

    // miscellaneous other variables for runtime
    public boolean registered = false;
    public String command = "";
    public ArrayList<String> aliases = new ArrayList<String>();

    // constructor without command specification
    public Command (OpenAuth instance) {
        plugin = instance;
        permissions = plugin.permissions;
        configuration = plugin.config;
        registry = plugin.registry;
        command = null;
    };

    // constructor with command specification
    public Command (OpenAuth instance, String command_root) {
        plugin = instance;
        permissions = plugin.permissions;
        configuration = plugin.config;
        registry = plugin.registry;
        command = command_root;
    };

    // methods
    public boolean addAlias (String alias) {
        /**
         * Registers an alias for the command.
         */
        try {
            if (registry.registerAlias(alias, this) == true)
                return aliases.add((String) alias);
        } catch (RegistryException e) {
            log.info("{Registry} Could not register alias '" + alias + "'.");
            return false;
        }
        return false;
    }

    public boolean deleteAlias (String alias)
      throws CommandException {
        /**
         * Deregisters an alias for the command.
         */
        try {
            if (registry.deregisterAlias(alias) == true)
                return aliases.remove((String) alias);
            else
                throw new CommandException(String.format("Alias [%s] does not exist.", alias));
        } catch (RegistryException e) {
            log.info("{Registry} Could not deregister alias '" + alias + "'.");
            return false;
        }
    }

    public boolean deleteAllAliases ()
      throws CommandException {
        /**
         * Deregisters all attached aliases.
         */
        Iterator alia_i = aliases.iterator();
        String a;
        while (alia_i.hasNext()) {
            a = (String) alia_i.next();
            try {
                registry.deregisterAlias((String) a);
            } catch (RegistryException e) {
                log.info("{Registry} Could not deregister alias '" + a + "'.");
                return false;
            };
            return true;
        };
        return true;
    };

    public boolean isRegistered () {
        /**
         * Returns whether or not this command is already in the global registry.
         */
        return (Boolean) registered;
    }

    public String getCommand () {
        /**
         * Returns the command that this instantiation is created for.
         */
        return (String) command;
    }

    public void setCommand (String cmd)
      throws CommandException {
        /**
         * This is only for in situations when the command has yet to be determined.
         * This command instance -MUST NOT- already be registered with the registry.
         */
        if (registered == true) {
            throw new CommandException("Command cannot be set with the command already registered.");
        }
        command = cmd;
    }

    public boolean register ()
      throws CommandException {
        /**
         * Register this command with the core registry and mark this
         * instance registered to prevent re-registration.
         */
        if (registered == true) {
            throw new CommandException("Command instance cannot be re-registered.");
        }
        try {
            registry.registerCommand(command, this);
        } catch (RegistryException e) {
            log.info("{Registry} Could not register command '" + command + "'.");
            registered = false;
            return false;
        }
        registered = true;
        return true;
    }

    public boolean deregister ()
      throws CommandException {
        /**
         * Deregister this command from the core registry
         */
        if (registered == false)
            throw new CommandException("Command instance is not registered.");
        try {
            registry.deregisterCommand(command);
        } catch (RegistryException e) {
            registered = true;
            return false;
        }
        registered = false;
        return true;
    }

    public boolean run (Player player, String[] args)
      throws CommandException {
        /**
         * This is where the code to be run when this command is process will be placed.
         * This must be overridden in another class file containing the command definition.
         * This method must not process subcommands, subcommands (eg., /<root> <subc>) must be processed with
         *     subcommand handler.
         */
        if (registered == false) {
            throw new CommandException("Command is not registered.");
        }
        return false;
    }
}
