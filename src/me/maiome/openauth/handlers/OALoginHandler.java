package me.maiome.openauth.handlers;

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

public interface OALoginHandler {

    List<OAPlayer> active;
    OpenAuth plugin;

    String getStringHash(String string);

    boolean isPlayerLoggedIn(OAPlayer player);
    boolean isPlayerLoggedIn(String player);

    boolean processPlayerLogin(OAPlayer player, String password);
    boolean processPlayerLogout(OAPlayer player);

    List<OAPlayer> getActivePlayers();

}