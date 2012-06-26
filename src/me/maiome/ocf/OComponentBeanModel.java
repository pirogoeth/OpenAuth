package me.maiome.ocf;

/**
 * This interface will serve as the base for the bean model.
 */

public abstract class OComponentBeanModel {

    /**
     * The getParent() method returns the parent class that contains the Class implementing this model.
     */
    public Class getParent();

    /**
     * These are methods that should be created in the inheritor for easy saving, updating, and removal.
     */
    public void save();
    public void update();
    public void delete();

}