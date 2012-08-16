package me.maiome.openauth.mixins;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.cl.GenericURIClassLoader;
import me.maiome.openauth.util.LogHandler;

import java.io.File;
import java.lang.reflect.*;
import java.util.*;

public class MixinManager {

    private final Map<String, IMixin> mixin_map = new HashMap<String, IMixin>();
    private final List<IMixin> mixins = new ArrayList<IMixin>();
    private final GenericURIClassLoader<IMixin> loader;
    public static MixinManager instance;
    private static final LogHandler log = new LogHandler();
    private static final String directory = "plugins/OpenAuth/mixins/";

    public MixinManager() {
        instance = this;
        this.loader = new GenericURIClassLoader<IMixin>(directory, IMixin.class);
        File parent = new File(directory);
        parent.mkdir();
    }

    public void load() {
        this.mixins.addAll(this.loader.load());
        for (IMixin mixin : this.mixins) {
            log.info("Loaded mixin: " + mixin.getName());
            mixin.onInit();
        }
    }

    public void unload() {
        for (IMixin mixin : this.mixins) {
            mixin.onTeardown();
            log.info("Unloaded mixin: " + mixin.getName());
        }
    }
}