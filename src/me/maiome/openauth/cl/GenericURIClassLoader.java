package me.maiome.openauth.cl;

import java.io.File;
import java.net.*;
import java.util.*;

import me.maiome.openauth.util.LogHandler;

/**
 * This is a URI class loader that loads and instantiates all classes out of a
 * specified directory. The whole point of the generic constraint is to
 * work with a certain function, and be able to effectively reuse the classloader
 * without having to rewrite it twenty million times.
 *
 * Usage:
 *
 *   GenericURIClassLoader<IPolicy> loader = new GenericURIClassLoader<IPolicy>("policies", IPolicy.class);
 *
 */
public class GenericURIClassLoader<T> {

    private static final LogHandler log = new LogHandler();
    private String directory = "";
    private Class clazz;

    public GenericURIClassLoader(String directory, Class clazz) {
        this.directory = directory;
        // this is the class all files we're loading SHOULD extend or implement.
        this.clazz = clazz;
    }

    public List<T> load() {
        List<T> classes = new ArrayList<T>();
        File dir = new File(directory);

        // make sure the directory is there.
        if (!(dir.exists())) {
            log.warning("{CL} Directory " + this.directory + " does not exist.");
            return classes;
        }

        // use a URLClassLoader to load the classes from the dir.
        ClassLoader loader;
        try {
            loader = new URLClassLoader(new URL[] { dir.toURI().toURL() }, this.clazz.getClassLoader());
        } catch (java.lang.Exception e) {
            log.warning("{CL} Encountered an error while initialising the class loader.");
            e.printStackTrace();
            return classes;
        }

        // classloading logic
        for (File f : dir.listFiles()) {
            if (!(f.getName().endsWith(".class"))) { // we only want to load .class files
                continue;
            }
            // get the filename without the .class
            String fname = f.getName().substring(0, f.getName().lastIndexOf("."));
            // finish loading the class.
            try {
                Class<?> clazz = loader.loadClass(fname);
                Object ob = clazz.newInstance();
                try {
                    this.clazz.cast(ob);
                } catch (java.lang.ClassCastException e) {
                    log.warning(clazz.getSimpleName() + " is not a class that extends or implements " + this.clazz.getCanonicalName());
                    continue;
                } catch (java.lang.Exception e) {
                    log.warning("Encountered an error while trying to cast " + clazz.getSimpleName() + " to " + this.clazz.getSimpleName() + ".");
                    e.printStackTrace();
                    continue;
                }
                T clazzz = (T) ob;
                classes.add(clazzz);
                log.info("Loaded class: " + clazzz.getClass().getSimpleName());
            } catch (java.lang.Exception e) {
                log.info("Error loading " + fname + ".");
                e.printStackTrace();
            }
        }
        return classes;
    }
}