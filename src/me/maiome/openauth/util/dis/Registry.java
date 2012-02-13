package me.maiome.openauth.util;

// Java imports
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

// Bukkit imports
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

// Core imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.util.Command;
import me.maiome.openauth.util.RegistryException;
import me.maiome.openauth.util.LogHandler;

public class Registry {
    // important core registry variables.
    public Map<String, Object> command = new HashMap<String, Object>();
    public Map<String, Object> aliases = new HashMap<String, Object>();
    public final LogHandler log = new LogHandler();
    public OpenAuth plugin;
    // constructor
    public Registry (OpenAuth instance) {
        plugin = instance;
    }
    // core registry utilities
    public Map<String, Object> getCommandMap () {
        /**
         * Returns the command map for raw outside manipulation.
         *
         * [accepts: none; returns: Map<String, Object>]
         */
        return command;
    }
    public boolean containsKey (String K) {
        /**
         * Checks if key K is in the command map.
         *
         * [accepts: (String) K; returns boolean]
         */
        return (Boolean) command.containsKey((String) K);
    }
    public boolean containsValue (Object V) {
        /**
         * Checks if value V exists in the command map.
         *
         * [accepts: (Object) V; returns boolean]
         */
        return (Boolean) command.containsValue((Object) V);
    }
    public int size () {
        /**
         * Returns the size of the command map.
         *
         * [accepts: none; returns: int]
         */
        return command.size();
    }
    public Object get (String K) {
        /**
         * Gets key-value item corresponding to K from the
         *     command map.
         *
         * [accepts: (Object) K; returns: (Object) V]
         */
        return (Object) command.get((String) K);
    }
    public Object put (String K, Object V) {
        /**
         * Puts a key-value pair into the command map.
         * This is not meant to be a replacement for the
         *     `register` method.
         *
         * [accepts: (Object) K, (Object) V;
         *      returns: previous V or null]
         */
        return (Object) command.put((String) K, (Object) V);
    }
    public Object remove (Object K) {
        /**
         * Removes K from the command map and returns
         *     the previous value for K, or null, if
         *     no such value existed.
         *
         * [accepts: (Object) K; returns: Object]
         */
        return (Object) command.remove((Object) K);
    }
    public boolean isEmpty () {
        /**
         * Returns whether or not the command map is empty.
         *     useful for sanity checking.
         *
         * [accepts: none; returns: boolean]
         */
        return command.isEmpty();
    }
    public Set<String> getCommandSet () {
        /**
         * Returns a Set which contains the keys of the
         *     command map.
         *
         * [accepts: none; returns: Set<String>]
         */
        return (Set<String>) command.keySet();
    }
    // core registration methods
    public boolean registerCommand (String commandLabel, Command commandInst)
      throws RegistryException {
        /**
         * Allows for the registering of a command in the
         *     registry's internal map to be process by the main onCommand method.
         *
         * [accepts: (String) commandLabel, (Command) commandInst; returns: boolean]
         */
        // is this command already registered?
        if (containsKey((String) commandLabel))
            throw new RegistryException(String.format("Command [%s] is already registered.", commandLabel));
        // does the map return null when we place the new value?
        if (put((String) commandLabel, (Object) commandInst) == null)
            return true;
        else
            return false;
    }
    public boolean deregisterCommand (String commandLabel)
      throws RegistryException {
        /**
         * Allows for the deregistration of a command from the
         *     registry's internal map.
         *
         * [accepts: (String) commandLabel; returns: boolean]
         */
        // is the command actually registered?
        if (!(containsKey((String) commandLabel)))
            throw new RegistryException(String.format("Command [%s] is not registered.", commandLabel));
        // does the map actually return a value for commandLabel, or does it return null?
        if (remove((String) commandLabel) == null || remove((String) commandLabel) != null)
            return true;
        else
            return false;
    }
    public void deregisterAll () {
        /**
         * Allows for the deregistration of all registered
         *     commands in a quick and easy fashion.
         *
         * [accepts: none; returns: none]
         */
        // well, this is boring.
        try {
            for (Map.Entry pair : command.entrySet()) {
                ((Command) pair.getValue()).deregister();
                ((Command) pair.getValue()).deleteAllAliases();
            }
        } catch (CommandException e) {
            log.warning("{Registry} Error deregistering all commands.");
        }
        return;
    }
    public boolean registerAlias (String aliasLabel, Command instantiation)
      throws RegistryException {
        /**
         * Allows for the registration of aliases for commands.
         * Basically sort of an alternative to normal core registration, but
         *    only should be used by a command instantiation after it has
         *    registered a main command.
         *
         * [accepts: (String) aliasLabel, (Command) instantiation;
         *  returns: boolean]
         */
        if (aliases.containsKey((String) aliasLabel))
            throw new RegistryException(String.format("Command [%s] is already registered.", aliasLabel));
        // does the map return null when we place the new value?
        if (aliases.put((String) aliasLabel, (Object) instantiation) == null)
            return true;
        else
            return false;
    }
    public boolean deregisterAlias (String aliasLabel)
      throws RegistryException {
        /**
         * Allows for the deregistration of aliases for commands.
         * Basically sort of an alternative to normal core registration, but
         *    only should be used by a command instantiation after it has
         *    registered a main command.
         *
         * [accepts: (String) aliasLabel; returns: boolean]
         */
        if (!(aliases.containsKey((String) aliasLabel)))
            throw new RegistryException(String.format("Command [%s] is not registered.", aliasLabel));
        // does the map actually return a value for commandLabel, or does it return null?
        if (aliases.remove((String) aliasLabel) == null || aliases.remove((String) aliasLabel) != null)
            return true;
        else
            return false;
    }
    public Command process (String commandLabel) {
        /**
         * Processes the incoming command and returns the
         *     value for commandLabel, if it exists.
         *
         * [accepts: (String) commandLabel; returns: Command]
         */
        // does this key even exist?
        if (containsKey((String) commandLabel))
            return (Command) get((String) commandLabel);
        else if (aliases.containsKey((String) commandLabel))
            return (Command) aliases.get((String) commandLabel);
        else
            return null;
    }
};