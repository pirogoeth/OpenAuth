package me.maiome.openauth.util;

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
 *   GenericClassLoader<T> loader = new GenericClassLoader<T>(String abstractPathToClasses, Class clazz);
 *
 *   GenericClassLoader<IMixin> loader = new GenericClassLoader<IMixin>("mixins", IMixin.class);
 *   List<IMixin> mixins = loader.load().getInstances();
 *     ==OR==
 *   GenericClassLoader<IMixin> loader = new GenericClassLoader<IMixin>("mixins", IMixin.class);
 *   List<Class> mixinClasses = loader.load().getClasses();
 *
 * Caveats:
 *
 *  Class must have a constructor that accepts no arguments to be instantiated.
 *
 */
public class GenericClassLoader<T> {

    // log handler, obviously
    private static final LogHandler log = new LogHandler();
    // directory we're going to be working in
    private String directory = "";
    // class to compare and cast to
    private Class clazz;
    // list of instances
    private final List<T> instances = new ArrayList<T>();
    // list of class objects
    private final List<Class> classes = new ArrayList<Class>();
    // list of files loaded
    private final Map<Class, File> files = new HashMap<Class, File>();

    /**
     * Obviously this is a constructor.
     * Takes two arguments.
     * Directory to load from, and target class, which should be an interface or an abstract class.
     */
    public GenericClassLoader(String directory, Class clazz) {
        this.directory = directory;
        // this is the class all files we're loading SHOULD extend or implement.
        this.clazz = clazz;
    }

    /**
     * Class loading logic is performed here.
     */
    public GenericClassLoader load() {
        List<T> classes = new ArrayList<T>();
        File dir = new File(directory);

        // make sure the directory is there.
        if (!(dir.exists())) {
            log.exDebug("{CL} Directory " + this.directory + " does not exist.");
            return null;
        }

        // use a URLClassLoader to load the classes from the dir.
        ClassLoader loader;
        try {
            loader = new URLClassLoader(new URL[] { dir.toURI().toURL() }, this.clazz.getClassLoader());
        } catch (java.lang.Exception e) {
            log.exDebug("{CL} Encountered an error while initialising the class loader.");
            e.printStackTrace();
            return null;
        }

        // classloading logic
        for (File f : dir.listFiles()) {
            if (!(f.getName().endsWith(".class"))) { // we only want to load .class files
                continue;
            }
            if (this.files.containsValue(f)) {
                // this file has already been loaded...
                continue;
            }
            // get the filename without the .class
            String fname = f.getName().substring(0, f.getName().lastIndexOf("."));
            // finish loading the class.
            try {
                Class clazz = loader.loadClass(fname);
                Object ob = clazz.newInstance();
                try {
                    this.clazz.cast(ob);
                } catch (java.lang.ClassCastException e) {
                    log.exDebug(clazz.getSimpleName() + " is not a class that extends or implements " + this.clazz.getCanonicalName());
                    continue;
                } catch (java.lang.Exception e) {
                    log.exDebug("Encountered an error while trying to cast " + clazz.getSimpleName() + " to " + this.clazz.getSimpleName() + ".");
                    e.printStackTrace();
                    continue;
                }
                T clazzz = (T) ob;
                classes.add(clazzz);
                this.classes.add(clazz); // class object
                this.instances.add(clazzz); // object instance
                this.files.put(clazz, f); // file instance
            } catch (java.lang.Exception e) {
                log.exDebug("Error loading " + fname + ".");
                e.printStackTrace();
            }
        }
        return this;
    }

    /**
     * Unloads a class from the the class loaders internals.
     */
    public void unload(Object obj) {
        Class clazz = obj.getClass();
        String filePath = directory + clazz.getName() + ".java";
        File file = new File(filePath);
        this.instances.remove(obj);
        this.classes.remove(clazz);
        this.files.remove(clazz);
    }

    /**
     * Returns all instances of the loaded classes.
     */
    public List<T> getInstances() {
        return this.instances;
    }

    /**
     * Returns all loaded class objects.
     */
    public List<Class> getClasses() {
        return this.classes;
    }

    /**
     * Returns all files that have been loaded by the loader.
     */
    public Collection<File> getFiles() {
        return this.files.values();
    }
}