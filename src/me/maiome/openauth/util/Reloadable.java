package me.maiome.openauth.util;

import java.util.*;

public abstract class Reloadable {

    private static final List<Reloadable> reloadables = new ArrayList<Reloadable>();

    /**
     * Iterates through our reloadables and calls reload() on each of them.
     */
    public static void requestReload() {
        for (Reloadable obj : reloadables) {
            obj.reload();
        }
    }

    public Reloadable() {
    }

    /**
     * Adds the class to the list of reloadables.
     */
    protected void setReloadable(Reloadable obj) {
        reloadables.add(obj);
    }

    /**
     * This method should be implemented by each child with their own way to reload their "reloadable" values.
     */
    protected abstract void reload();
}