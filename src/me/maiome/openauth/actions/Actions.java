package me.maiome.openauth.actions;

// java imports
import java.io.File;
import java.lang.reflect.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

// internal imports
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.cl.*;
import me.maiome.openauth.metrics.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;

public enum Actions {

    BAN(BanStick.class),
    BOOM(BoomStick.class),
    BOX(BoxStick.class),
    FREEZE(FreezeStick.class),
    HOUNDS(HellHounds.class),
    SPAWN(SpawnStick.class);

    static {
        File f = new File("plugins/OpenAuth/actions/");
        f.mkdir();
    }

    public final Class action;
    private final static LogHandler log = new LogHandler();
    private final static Class[] action_cons_types = {OAServer.class, Session.class};
    private static final Map<String, Class> store = new HashMap<String, Class>();
    private static final GenericURIClassLoader<IAction> classLoader = new GenericURIClassLoader<IAction>("plugins/OpenAuth/actions/", IAction.class);

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
            store.put((String) a.getField("name").get(a), a);
            log.exDebug(String.format("Action %s (%s) was registered.", (String) a.getField("name").get(a), a.getCanonicalName()));
            try {
                Tracker metric = (Tracker) a.getField("tracker").get(a);
                if (metric != null) {
                    OpenAuth.getMetrics().addCustomData(metric);
                }
                log.exDebug(String.format("Registered Metrics data tracker [%s] from %s.", metric.getColumnName(), a.getCanonicalName()));
            } catch (java.lang.Exception e) {
                log.info("Exception occurred while registering Action data tracker.");
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
            log.exDebug(String.format("Action %s (%s) was purged.", (String) a.getField("name").get(a), a.getCanonicalName()));
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
                a = (IAction) c.newInstance(attachable.getServer(), attachable);
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
            return (IAction) c.newInstance(attachable.getServer(), attachable);
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
     * Returns a full list of registered Actions.
     */
    public static Set<String> getActions() {
        return (Set<String>) store.keySet();
    }
}