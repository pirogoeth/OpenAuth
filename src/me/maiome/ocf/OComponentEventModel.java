package me.maiome.ocf;

import org.bukkit.event.Listener;

/**
 * This interface will serve as the base for the event model.
 */

public abstract class OComponentEventModel extends Listener {

    /**
     * The getParent() method returns the parent class that contains the Class implementing this model.
     */
    public Class getParent();

}