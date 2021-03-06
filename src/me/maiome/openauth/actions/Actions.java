package me.maiome.openauth.actions;

// java imports
import java.io.File;
import java.lang.reflect.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

// internal imports
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.metrics.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.GenericClassLoader;
import me.maiome.openauth.util.LogHandler;

public enum Actions {

    BAN(BanStick.class),
    BOOM(BoomStick.class),
    BOX(BoxStick.class),
    FREEZE(FreezeStick.class),
    // HOUNDS(HellHounds.class),
    SPAWN(SpawnStick.class);

    static {
        File f = new File("plugins/OpenAuth/actions/");
        f.mkdir();
    }

    public final Class action;
    private final static LogHandler log = new LogHandler();
    private final static Class[] action_cons_types = {Session.class};
    private static final Map<String, Class> store = new HashMap<String, Class>();
    private static final GenericClassLoader<IAction> classLoader = new GenericClassLoader<IAction>("plugins/OpenAuth/actions/", IAction.class);

    Actions(final Class action) {
        this.action = action;
    }

    /**
     * Returns the Class for the current action.
     */
    public Class getAction() {
        return this.action;
    }

    /**
     * This allows an external plugin to register an Action.
     *
     * Example:
     *   import me.maiome.openauth.actions.Actions;
     *   ...
     *   Actions.registerAction(ShitStick.class);
     */
    public static void registerAction(final Class a) {
        // if (!(a.isAssignableFrom(IAction.class))) return;
        try {
            store.put((String) a.getField("name").get(null), a);
            log.debug(String.format("Action %s (%s) was registered.", (String) a.getField("name").get(null), a.getCanonicalName()));
            try {
                Tracker metric = (Tracker) a.getField("tracker").get(null);
                try {
                    OpenAuth.getMetrics().addCustomData(metric);
                    log.debug(String.format("Registered Metrics data tracker [%s] from %s.", metric.getColumnName(), a.getCanonicalName()));
                } catch (java.lang.NullPointerException e) {
                    log.info("Action data tracker was null.");
                } catch (java.lang.Exception e) {
                    log.info("Exception occurred while adding Action data tracker.");
                    e.printStackTrace();
                }
            } catch (java.lang.Exception e) {
                log.info("Exception occurred while finding Action data tracker.");
                e.printStackTrace();
            }
        } catch (java.lang.Exception e) {
            log.info("Exception occurred while registering an Action.");
            e.printStackTrace();
        }
    }

    /**
     * This allows an external plugin to deregister/purge an Action.
     *
     * Example:
     *   import me.maiome.openauth.actions.Actions;
     *   ...
     *   Actions.purgeAction(ShitStick.class);
     */
    public static void purgeAction(final Class a) {
        if (!(a.isAssignableFrom(IAction.class))) return;
        try {
            store.remove((String) a.getField("name").get(a));
            log.debug(String.format("Action %s (%s) was purged.", (String) a.getField("name").get(a), a.getCanonicalName()));
        } catch (java.lang.Exception e) {
            log.info("Exception occurred while purging an Action.");
            e.printStackTrace();
        }
    }

    // Instantiates all internal actions when the class is loaded.
    public static void init() {
        for (Actions a : Actions.values()) {
            try {
                registerAction(a.getAction());
            } catch (java.lang.Exception e) {
                log.info("Exception occurred while initialising Actions enumerator.");
                e.printStackTrace();
            }
        }
        for (Object ob : classLoader.load().getClasses()) {
            Class c = null;
            try {
                c = (Class) ob;
            } catch (java.lang.Exception e) {
                log.info("Exception occurred while casting up external Action.");
                e.printStackTrace();
            }
            try {
                registerAction(c);
            } catch (java.lang.Exception e) {
                log.info("Exception occurred while registering external Action.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Instantiates an Action by the specified name.
     */
    public static IAction getActionByName(final String name, final Session attachable) {
        IAction a;
        try {
            if (store.get(name) != null) {
                Constructor c = (store.get(name)).getConstructor(action_cons_types);
                a = (IAction) c.newInstance(attachable);
            } else {
                return null;
            }
        } catch (java.lang.Exception e) {
            log.info("Exception caught while attaching an action.");
            e.printStackTrace();
            return null;
        }
        return a;
    }

    /**
     * Instantiates the specified Action.
     */
    public IAction getInstance(Session attachable) {
        try {
            Constructor c = this.action.getConstructor(action_cons_types);
            return (IAction) c.newInstance(attachable);
        } catch (java.lang.Exception e) {
            log.info("Exception caught while instantiating an action.");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tells whether an Action exists or not.
     */
    public static boolean actionExists(String name) {
        return store.containsKey(name);
    }

    /**
     * Returns a full list of registered Actions (names).
     */
    public static Set<String> getActions() {
        return (Set<String>) store.keySet();
    }
}