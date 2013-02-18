package me.maiome.openauth.mixins;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.util.GenericClassLoader;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.ReflectionUtil;

import java.io.File;
import java.lang.reflect.*;
import java.util.*;

import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.event.Listener;
import org.bukkit.command.SimpleCommandMap;

public class MixinManager {

    private GenericClassLoader<IMixin> loader;
    private final List<IMixin> mixins = new ArrayList<IMixin>();
    private static MixinManager instance;
    private static final LogHandler log = new LogHandler();
    private static final String directory = "plugins/OpenAuth/mixins/";

    public static MixinManager getInstance() {
        return instance;
    }

    public MixinManager() {
        instance = this;
        this.loader = new GenericClassLoader<IMixin>(directory, IMixin.class);
        File parent = new File(directory);
        parent.mkdir();
    }

    public void load() {
        List<IMixin> newMixins = this.loader.load().getInstances();
        for (IMixin mixin : newMixins) {
            if (this.mixins.contains(mixin)) {
                continue;
            }
            this.mixins.add(mixin);
            log.info("Loaded mixin: " + mixin.getName());
            mixin.onInit();
        }
    }

    public void reload() {
        this.unload();
        this.loader = new GenericClassLoader<IMixin>(directory, IMixin.class);
        this.load();
    }

    public void unload() {
        for (IMixin mixin : this.mixins) {
            mixin.onTeardown();
            this.loader.unload(mixin);
            log.info("Unloaded mixin: " + mixin.getName());
        }
        this.mixins.clear();
    }

    public void unload(IMixin obj) {
        obj.onTeardown();
        this.loader.unload(obj);
        log.info("Unloaded mixin: " + obj.getName());
        this.mixins.remove(obj);
    }

    protected CommandMap getCommandMap() {
        CommandMap commandMap = ReflectionUtil.getField(OpenAuth.getOAServer().getServer().getPluginManager(), "commandMap");
        if (commandMap == null) {
            commandMap = new SimpleCommandMap(OpenAuth.getOAServer().getServer());
        }
        return commandMap;
    }
}