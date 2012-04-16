package me.maiome.openauth.actions;

// java imports
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.lang.reflect.*;

// internal imports
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;

public enum Actions {

    BAN(BanStick.class),
    BOOM(BoomStick.class),
    FREEZE(FreezeStick.class),
    SPAWN(SpawnStick.class);

    public final Class action;
    private final static LogHandler log = new LogHandler();
    private final static Class[] action_cons_types = {OAServer.class, Session.class};
    private static final Map<String, Class> store = new HashMap<String, Class>();

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

    // Instantiates all internal actions.

    static {
        for (Actions a : Actions.values()) {
            try {
                registerAction(a.getAction());
            } catch (java.lang.Exception e) {
                log.info("Exception occurred while initialising Actions enumerator.");
                e.printStackTrace();
            }
        }
    }
}