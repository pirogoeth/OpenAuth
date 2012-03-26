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
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.Permission;

public interface Action {

    // fields
    String name = null;

    // methods
    boolean allowed();
    boolean isUsed();
    boolean requiresEntityTarget();
    boolean allowsAnyEntityTarget();

    void setSender(final OAPlayer sender);

    void run(final Entity entity);
    void run(final OAPlayer player);
    void run(final Block block);

    void undo();

}