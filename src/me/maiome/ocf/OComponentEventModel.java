package me.maiome.ocf;

/**
 * This interface will serve as the base for the event model.
 */

public abstract class OComponentEventModel {

    /**
     * The getParent() method returns the parent class that contains the Class implementing this model.
     */
    public abstract Class getParent();
}