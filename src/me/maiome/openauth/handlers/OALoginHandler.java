package me.maiome.openauth.handlers;

// bukkit
import org.bukkit.event.player.PlayerLoginEvent;

// java
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

// internal
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.Permission;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.LoginStatus;

public interface OALoginHandler {

    /**
     * Generate MD5 string hash of a specified password.
     */
    String getStringHash(String password);

    /**
     * Checks if a player is logged in or not.
     */
    boolean isPlayerLoggedIn(OAPlayer player);
    boolean isPlayerLoggedIn(String player);

    /**
     * Checks if a user is registered or not.
     */
    boolean isRegistered(OAPlayer player);
    boolean isRegistered(String player);

    /**
     * Retrieve the LoginStatus of a player.
     */
    LoginStatus getPlayerStatus(OAPlayer player);

    /**
     * Processes player logins.
     */
    void processPlayerLogin(PlayerLoginEvent event, OAPlayer player);

    /**
     * Processes player logouts.
     */
    void processPlayerLogout(OAPlayer player);

    /**
     * Processes player identification tries.
     */
    boolean processPlayerIdentification(OAPlayer player, String password);

    /**
     * Processes player registration.
     */
    void processPlayerRegistration(OAPlayer player, String password);
    void processPlayerRegistration(String player, String password);

    boolean compareToCurrent(OAPlayer player, String password);

    /**
     * Returns all active (logged in) players.
     */
    List<OAPlayer> getActivePlayers();

}
