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
    private final Map<String, IMixin> mixins = new HashMap<String, IMixin>();
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
        new MixinLibraryLoader();
    }

    public void load() {
        List<IMixin> newMixins = this.loader.load().getInstances();
        for (IMixin mixin : newMixins) {
            if (this.mixins.containsValue(mixin)) {
                continue;
            }
            this.mixins.put(mixin.getName(), mixin);
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
        for (IMixin mixin : this.mixins.values()) {
            mixin.onTeardown();
            this.loader.unload(this.mixins.get(mixin.getName()));
            log.info("Unloaded mixin: " + mixin.getName());
        }
        this.mixins.clear();
    }

    public void unload(IMixin obj) {
        obj.onTeardown();
        this.loader.unload(this.mixins.get(obj.getName()));
        this.mixins.remove(obj.getName());
        log.info("Unloaded mixin: " + obj.getName());
    }
}