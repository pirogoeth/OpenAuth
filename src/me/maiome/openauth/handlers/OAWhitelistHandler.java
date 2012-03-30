package me.maiome.openauth.handlers;

// internal
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.util.*;

// bukkit
import org.bukkit.entity.Player;

// java
import java.util.List;

public interface OAWhitelistHandler {

    void setEnabled(boolean b);
    boolean isEnabled();

    /**
     * Checks if the specified player is whitelisted or not.
     */
    boolean isWhitelisted(OAPlayer player);
    /**
     * Checks if the specified player is whitelisted or not.
     */
    boolean isWhitelisted(Player player);

    /**
     * Returns the WhitelistStatus of the specified player.
     */
    WhitelistStatus getPlayerStatus(OAPlayer player);

    /**
     * When a user joins, this checks if they are whitelisted and kicks them if not.
     */
    void processPlayerJoin(OAPlayer player);

    /**
     * Writes the whitelist to the appropriate configuration.
     */
    void saveWhitelist();
    /**
     * Loads the whitelist from the appropriate configuration.
     */
    void loadWhitelist();
    /**
     * Overloaded method to whitelist a player.
     */
    void whitelistPlayer(OAPlayer player);
    void whitelistPlayer(Player player);
    void whitelistPlayer(String name);
    /**
     * Overloaded method to unwhitelist a player
     */
    void unwhitelistPlayer(OAPlayer player);
    void unwhitelistPlayer(Player player);
    void unwhitelistPlayer(String name);
    /**
     * Returns a string list of whitelisted players.
     */
    List<String> getWhitelist();

}