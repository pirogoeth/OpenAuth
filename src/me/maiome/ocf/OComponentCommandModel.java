package me.maiome.ocf;

/**
 * This interface will serve as the base for the command model.
 */

public abstract class OComponentCommandModel {

    /**
     * The getParent() method returns the parent class that contains the Class implementing this model.
     */
    public Class getParent();

}