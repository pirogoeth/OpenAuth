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
    BOOM(BoomStick.class);

    public final Class action;
    private final static LogHandler log = new LogHandler();
    private final static Class[] action_cons_types = {OAServer.class, Session.class};
    private static final Map<String, Class> store = new HashMap<String, Class>();

    Actions(final Class action) {
        this.action = action;
    }

    public Class getAction() {
        return this.action;
    }

    public static Action getActionByName(final String name, final Session attachable) {
        Action a;
        try {
            if (store.get(name) != null) {
                Constructor c = (store.get(name)).getConstructor(action_cons_types);
                a = (Action) c.newInstance(attachable.getServer(), attachable);
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

    protected Action getInstance(Session attachable) {
        try {
            Constructor c = this.action.getConstructor(action_cons_types);
            return (Action) c.newInstance(attachable.getServer(), attachable);
        } catch (java.lang.Exception e) {
            log.info("Exception caught while instantiating an action.");
            e.printStackTrace();
            return null;
        }
    }

    public static boolean actionExists(String name) {
        return store.containsKey(name);
    }

    public static Set<String> getActions() {
        return (Set<String>) store.keySet();
    }

    static {
        for (Actions a : Actions.values()) {
            try {
                store.put((String) (a.getAction()).getField("name").get(a.getAction()), a.getAction());
            } catch (java.lang.Exception e) {
                log.info("Exception occurred while initialising Actions enumerator.");
                e.printStackTrace();
            }
        }
    }
}