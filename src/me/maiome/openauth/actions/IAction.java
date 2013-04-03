package me.maiome.openauth.actions;

// bukkit imports
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.session.Session;
import me.maiome.openauth.session.SessionController;
import me.maiome.openauth.metrics.Tracker;
import me.maiome.openauth.util.*;

public interface IAction {

    // fields
    /**
     * Name of the action.
     */
    String name = null;
    /**
     * Stores args passed to the action by the player.
     */
    String[] args = null;
    /**
     * Holds the Metrics tracker object.
     */
    Tracker tracker = null;

    // methods
    /**
     * Returns the name of the action.
     */
    String getName();
    /**
     * Returns whether the session this action is currently attached to is allowed
     * to use this action.
     */
    boolean allowed();
    /**
     * Returns whether or not this action has been used.
     */
    boolean isUsed();
    /**
     * Returns whether this action must be used on an entity target, or
     * if block targets are acceptible.
     */
    boolean requiresEntityTarget();
    /**
     * Returns whether this action will even accept an entity target.
     */
    boolean allowsAnyEntityTarget();
    /**
     * Returns if this action accepts arguments from the player.
     */
    boolean allowsArgs();
    /**
     * Returns if this action has arguments set.
     */
    boolean hasArgs();
    /**
     * Returns if this action requires arguments to run.
     */
    boolean requiresArgs();
    /**
     * Returns the arguments that this action was passed.
     */
    String[] getArgs();

    /**
     * Sets the using player of the action.
     */
    void setSender(final OAPlayer sender);
    /**
     * Sets the args of the action.
     */
    void setArgs(String[] args);

    /**
     * Runs the action on an Entity target.
     */
    void run(final Entity entity);
    /**
     * Runs the action on an OAPlayer target.
     */
    void run(final OAPlayer player);
    /**
     * Runs the action on a Block target.
     */
    void run(final Block block);

    /**
     * Performs an undo of the action, if there is one available.
     */
    void undo();

}