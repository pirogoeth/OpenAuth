package me.maiome.ocf;

/**
 * This abstract class will serve as the base for the bean model.
 */

public abstract class OComponentBeanModel {

    /**
     * The getParent() method returns the parent class that contains the Class implementing this model.
     */
    public abstract Class getParent();

    /**
     * These are methods that should be overridden in the inheritor for easy saving, updating, and removal.
     */
    public abstract void save();
    public abstract void update();
    public abstract void delete();

}