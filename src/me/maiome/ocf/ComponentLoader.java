package me.maiome.ocf;

import java.io.File;
import java.io.InputStream;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.persistence.*;

import com.sk89q.bukkit.util.*;
import com.sk89q.util.*;
import com.sk89q.minecraft.util.commands.*;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.avaje.ebean.EbeanServer;

import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.database.ExtendedDB;
import me.maiome.openauth.util.Permission;

/**
 * This is the actual component loader code, obviously.
 * All the component checking and registering will go on in here.
 */

public class ComponentLoader {

    private class LogManager {
        private final Logger logger = Logger.getLogger("Minecraft");
        private final String prefix = "OComponentLoader";

        public void info(String...messages) {
            for (String str : messages) {
                logger.info(String.format("[%s] %s", prefix, str));
            }
        }

        public void warning(String...messages) {
            for (String str : messages) {
                logger.warning(String.format("[%s] %s", prefix, str));
            }
        }

        public void severe(String...messages) {
            for (String str : messages) {
                logger.severe(String.format("[%s] %s", prefix, str));
            }
        }
    }

    private final static String resourcedir = "plugins" + File.separator + "OComponentFramework";
    protected final static Map<String, ComponentLoader> instances = new HashMap<String, ComponentLoader>();
    private CommandsManager<CommandSender> cmgr;
    private CommandsManagerRegistration cmgreg;
    private Map<String, Class<?>> components = new HashMap<String, Class<?>>(); // map of ALL components
    private List<Class> events = new ArrayList<Class>(), beans = new ArrayList<Class>(), commands = new ArrayList<Class>(); // components
    private static Map<Class<?>, List<?>> entities = new HashMap<Class<?>, List<?>>();
    private static Map<Class, EbeanServer> databases = new HashMap<Class, EbeanServer>();
    private final Class[] comp_cons_types = {JavaPlugin.class};
    private JavaPlugin plugin;
    private LogManager log = new LogManager();

    public ComponentLoader(JavaPlugin plugin) {
        // declare the javaplugin we're running for.
        this.plugin = plugin;

        // setup objects for bridging command classes.
        this.cmgr = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender sender, String perm) {
                if (sender instanceof org.bukkit.command.ConsoleCommandSender) {
                    return true;
                }
                return Permission.has((Player) sender, perm);
            }
        };

        // command registry
        this.cmgreg = new CommandsManagerRegistration(plugin, this.cmgr);

        // register the instance
        setInstance(plugin, this);
    }

    public static void setInstance(JavaPlugin plugin, ComponentLoader cl) {
        if (instances.containsKey(plugin.getDescription().getName())) {
            throw new UnsupportedOperationException("Can't overwrite an existing ComponentLoader instance.");
        }
        instances.put(plugin.getDescription().getName(), cl);
    }

    public static ComponentLoader getInstance(JavaPlugin plugin) {
        return getInstance(plugin.getDescription().getName());
    }

    public static ComponentLoader getInstance(String plugin) {
        return getInstance(plugin);
    }

    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    public static List<Class<?>> getDBEntities(Class component) {
        return (List<Class<?>>) entities.get(component);
    }

    public static EbeanServer getDatabase(Class component) {
        return databases.get(component);
    }

    public ExtendedDB initDatabase(final Class component, OComponentBeanConfiguration config) {
        ExtendedDB database = new ExtendedDB(this.plugin) {
            @Override
            protected List<Class<?>> getDatabaseClasses() {
                return ComponentLoader.getInstance(plugin).getDBEntities(component);
            }
        };
        database.initializeDatabase(
            config.driver(),
            config.url().replaceAll("\\{OCROOT\\}", ComponentLoader.resourcedir).replaceAll("\\{COMPONENTNAME\\}", ((OComponent) component.getAnnotation(OComponent.class)).value()),
            config.username(),
            config.password(),
            config.isolation(),
            config.logging(),
            config.rebuild()
        );

        if (config.wal_mode() == true) {
            database.getDatabase().createSqlQuery("PRAGMA journal_mode=WAL");
        }

        databases.put(component, database.getDatabase());
        return database;
    }

    public void registerEventHandler(Class<Listener>...events) {
        for (Class<Listener> event : events) {
            try {
                Constructor c = event.getConstructor(this.comp_cons_types);
                this.plugin.getServer().getPluginManager().registerEvents(Listener.class.cast(c.newInstance(this.plugin)), this.plugin);
            } catch (java.lang.Exception e) {
                this.log.warning("An error occurred while registering an event listener!");
                e.printStackTrace();
            }
        }
    }

    public void registerCommands(Class<?> clazz) {
        this.cmgreg.register(clazz);
    }

    // component loading code.

    public void loadComponents(List<String> clazzlist) {
        for (String clazz : clazzlist) {
            this.loadComponent(clazz);
        }
    }

    public Class loadComponent(String clazz) {
        try {
            Class c = Class.forName(clazz);
            this.loadComponent(c);
            return c;
        } catch (java.lang.Exception e) {
            this.log.warning("An exception occurred while loading component [" + clazz + "]!");
            e.printStackTrace();
        }
        return null;
    }

    private void loadComponent(final Class<?> clazz) {
        // main component loading section.
        OComponent oc = clazz.getAnnotation(OComponent.class);
        if (oc == null) {
            this.log.severe("Rejecting class [" + clazz.getCanonicalName() + "]!", "Class is not a valid OComponent!");
            return;
        }
        this.log.info("Found component [" + clazz.getCanonicalName() + "], loading..");
        OComponentType oct = clazz.getAnnotation(OComponentType.class);
        if (oct == null) {
            this.log.severe("Rejecting class [" + clazz.getCanonicalName() + "!]", "Class is not a valid OComponent!", "(Class is missing the @OComponentType declaration..)");
            return;
        }
        // typed loading section (BEAN, COMMAND, etc)
        for (ComponentType type : oct.value()) {
            switch (type) {
                case BEAN:
                    OComponentBeanTarget ocbt = clazz.getAnnotation(OComponentBeanTarget.class);
                    OComponentBeanConfiguration ocbc = clazz.getAnnotation(OComponentBeanConfiguration.class);
                    if (ocbt == null || ocbt.value() == null) {
                        this.log.warning("Missing or malformed OComponentBeanTarget in class [" + clazz.getCanonicalName() + "]!", "Non-fatal error, using main component as target instead.");
                    } else if (ocbc == null) {
                        this.log.severe("Missing OComponentBeanConfiguration in class [" + clazz.getCanonicalName() + "]!");
                        break;
                    }
                    Class bt = null;
                    try {
                        if (ocbt != null && ocbt != null) {
                            bt = ocbt.value();
                        } else if (ocbt == null) {
                            bt = clazz;
                        }
                    } catch (java.lang.Exception e) {
                        this.log.severe("Problem while resolving OComponentBeanTarget in class [" + clazz.getCanonicalName() + "]!");
                        e.printStackTrace();
                        break;
                    }
                    if (OComponentBeanModel.class.isAssignableFrom(bt) && bt.getAnnotation(Entity.class) != null && bt != null) { // this proves its a bean class.
                        OComponentBeanEntities ocbe = clazz.getAnnotation(OComponentBeanEntities.class);
                        List<Class<OComponentBeanModel>> beans = new ArrayList<Class<OComponentBeanModel>>();
                        beans.add(bt);
                        if (ocbe != null) {
                            for (Class<OComponentBeanModel> bean : ocbe.value()) {
                                if (bean.getAnnotation(Entity.class) != null) beans.add(bean);
                            }
                        }
                        entities.put(clazz, beans); // puts the entities in a List<> for getDatabaseClasses()
                    } else { // i guess this means it isn't actually a OComponent bean, huh?
                        this.log.severe("Malformed OComponentBeanTarget in class [" + clazz.getCanonicalName() + "]!");
                        break;
                    }
                    this.beans.add(bt);
                    this.log.info("Registering OComponentBean [" + clazz.getCanonicalName() + "] in database!");
                    this.initDatabase(clazz, ocbc);
                case COMMAND:
                    OComponentCommandTarget occt = clazz.getAnnotation(OComponentCommandTarget.class);
                    if (occt == null || occt.value() == null) {
                        this.log.warning("Missing or malformed OComponentCommandTarget in class [" + clazz.getCanonicalName() + "]!", "Non-fatal error, using main component as target instead.");
                    }
                    Class ct = null;
                    try {
                        if (occt != null && occt.value() != null) {
                            ct = occt.value();
                        } else if (occt == null || occt.value() == null) {
                            ct = clazz;
                        }
                    } catch (java.lang.Exception e) {
                        this.log.severe("Problem while resolving OComponentCommandTarget in class [" + clazz.getCanonicalName() + "]!");
                        e.printStackTrace();
                        break;
                    }
                    if (ct != null) {
                        boolean valid = false;
                        for (Method m : ct.getMethods()) {
                            if (m.getAnnotation(Command.class) != null) { // yay, found an @Command annotation!
                                valid = true;
                                break;
                            }
                        }
                        if (valid) { // register
                            this.commands.add(ct);
                            this.log.info("Registering OComponent [" + clazz.getCanonicalName() + "] with command handler!");
                            this.registerCommands(ct);
                        } else {
                            this.log.severe("Could not find an @Command annotation in component [" + clazz.getCanonicalName() + "].");
                            break;
                        }
                    } else {
                        this.log.severe("Malformed OComponentCommand in class [" + clazz.getCanonicalName() + "]!");
                        break;
                    }
                case EVENT:
                    OComponentEventTarget ocet = clazz.getAnnotation(OComponentEventTarget.class);
                    if (ocet == null || ocet.value() == null) {
                        this.log.warning("Missing or malformed OComponentEventTarget in class [" + clazz.getCanonicalName() + "]!", "Non-fatal error, using main component as target instead.");
                    }
                    Class et = null;
                    try {
                        if (ocet != null && ocet.value() != null) {
                            et = ocet.value();
                        } else if (ocet == null || ocet.value() == null) {
                            et = clazz;
                        }
                    } catch (java.lang.Exception e) {
                        this.log.severe("Problem while resolving OComponentEventTarget in class [" + clazz.getCanonicalName() + "]!");
                        e.printStackTrace();
                        break;
                    }
                    if (Listener.class.isAssignableFrom(et) && et != null) {
                        boolean valid = false;
                        for (Method m : et.getMethods()) {
                            if (m.getAnnotation(EventHandler.class) != null) { // yay, found an @EventHandler annotation!
                                valid = true;
                                break;
                            }
                        }
                        if (valid) { // register
                            this.events.add(et);
                            this.log.info("Registering OComponent [" + clazz.getCanonicalName() + "] with event handler!");
                            this.registerEventHandler(et);
                        } else {
                            this.log.severe("Could not find an @EventHandler annotation in component [" + clazz.getCanonicalName() + "].");
                            break;
                        }
                    } else {
                        this.log.severe("Malformed OComponentEvent in class [" + clazz.getCanonicalName() + "]!");
                        break;
                    }
            }
        }
        if (oc.value().equals("[unnamed]")) {
            this.components.put(clazz.getSimpleName(), clazz);
        } else {
            this.components.put(oc.value(), clazz);
        }
    }
}