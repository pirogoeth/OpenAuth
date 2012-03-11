package me.maiome.openauth.actions;

// bukkit imports
import org.bukkit.Location;
import org.bukkit.World;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.session.Session;
import me.maiome.openauth.session.SessionController;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.Permission;

public interface Action {

    // fields
    Session attached;
    SessionController sc;
    String permissible;
    OAServer server;

    // methods
    boolean allowed();

    void run();

    void undo();

}