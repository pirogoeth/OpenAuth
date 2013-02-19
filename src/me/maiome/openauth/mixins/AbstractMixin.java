package me.maiome.openauth.mixins;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.util.*;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.event.*;

import com.sk89q.minecraft.util.commands.*;

import java.lang.reflect.*;
import java.util.*;

public abstract class AbstractMixin implements IMixin, Listener {

    protected final String name;
    protected OpenAuth controller;
    protected final static LogHandler log = new LogHandler();

    protected AbstractMixin(String name) {
        this.name = name;
        this.controller = (OpenAuth) OpenAuth.getInstance();
    }

    /**
     * Returns the mixin's name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * This is abstracted away to the end mixin-writer.
     * All setup and custom teardown will be up to them.
     */
    public abstract void onInit();

    /**
     * This will automatically perform most mixin teardown for you. If you are
     * registering with something else that may require teardown, in the case of a
     * mixin reload, you are responsible for that yourself. You must override this method,
     * but also use super.onTeardown() first.
     */
    public void onTeardown() {
        if (this.hasCommandMethods()) {
            this.unregisterCommands();
        }
        if (this.hasListenerMethods()) {
            this.unregisterListeners();
        }
    };

    /**
     * Uses reflection to get the command map currently being used by Bukkit.
     */
    private CommandMap getCommandMap() {
        CommandMap commandMap = ReflectionUtil.getField(Bukkit.getServer().getPluginManager(), "commandMap");
        if (commandMap == null) {
            this.log.warning("Was not able to fetch server command map, using a fallback instead!");
            commandMap = new SimpleCommandMap(Bukkit.getServer());
        }
        return commandMap;
    }

    /**
     * A shorter way to register the class as a command handler.
     */
    protected void registerCommands() {
        this.controller.getCommandsManagerRegistration().register(this.getClass());
    }

    /**
     * A much shorter and easier way to register the mixin's listeners.
     */
    protected void registerListeners() {
        this.controller.getServer().getPluginManager().registerEvents(this, OpenAuth.getInstance());
    }

    /**
     * Checks if the class has any methods annotated with @Command, meaning that there are command processors
     * in the class.
     */
    private boolean hasCommandMethods() {
        for (Method method : this.getClass().getMethods()) {
            com.sk89q.minecraft.util.commands.Command cmdAnno = method.getAnnotation(com.sk89q.minecraft.util.commands.Command.class);
            if (cmdAnno != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the class has any methods annotated with @EventHandler, meaning that there are listeners here.
     */
    private boolean hasListenerMethods() {
        for (Method method : this.getClass().getMethods()) {
            EventHandler evhAnno = method.getAnnotation(EventHandler.class);
            if (evhAnno != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Deregisters all command handlers.
     */
    private void unregisterCommands() {
        CommandMap commandMap = this.getCommandMap();
        Map<String, org.bukkit.command.Command> knownCommands = ReflectionUtil.getField(commandMap, "knownCommands");
        List<String> found = new ArrayList<String>();
        for (Method method : this.getClass().getMethods()) {
            com.sk89q.minecraft.util.commands.Command cmdAnno = method.getAnnotation(com.sk89q.minecraft.util.commands.Command.class);
            if (cmdAnno != null) {
                for (String alias : cmdAnno.aliases()) {
                    found.add(alias);
                }
            }
        }
        Map<Method, Map<String, Method>> commands = ReflectionUtil.getField(this.controller.getCommandsManager(), "commands");
        Map<String, Method> map = commands.get(null);
        List<String> toRemove = new ArrayList<String>();
        for (String cmd : found) {
            Method meth = map.get(cmd);
            if (meth != null) {
                toRemove.add(cmd);
            }
        }
        for (String cmd : toRemove) {
            if (knownCommands.containsKey(cmd)) {
                knownCommands.remove(cmd);
            }
            map.remove(cmd);
        }
        commands.put(null, map);
    }

    /**
     * Deregisters all listeners.
     */
    private void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }
}