package me.maiome.openauth.mixins;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.util.GenericClassLoader;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.ReflectionUtil;

import java.io.File;
import java.lang.reflect.*;
import java.util.*;

public class MixinLibraryLoader {

    private GenericClassLoader<IMixinLibraryProvider> loader;
    private final Map<String, IMixinLibraryProvider> libraries = new HashMap<String, IMixinLibraryProvider>();
    private static MixinLibraryLoader instance;
    private static final LogHandler log = new LogHandler();
    private static final String directory = "plugins/OpenAuth/mixins/";

    public static MixinLibraryLoader getInstance() {
        return instance;
    }

    public MixinLibraryLoader() {
        instance = this;
        File parent = new File(directory);
        parent.mkdir();
        this.loader = new GenericClassLoader<IMixinLibraryProvider>(directory, IMixinLibraryProvider.class);
        this.load();
    }

    private void load() {
        List<IMixinLibraryProvider> libraries = this.loader.load().getInstances();
        for (IMixinLibraryProvider library : libraries) {
            this.libraries.put(library.getName(), library);
            log.info("Loaded library: " + library.getName());
        }
    }

    public IMixinLibraryProvider getLibrary(String name) {
        return this.libraries.get(name);
    }

}