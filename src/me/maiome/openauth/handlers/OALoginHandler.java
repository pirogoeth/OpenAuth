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
import me.maiome.openauth.util.LoginStatus;

public interface OALoginHandler {

    void setEnabled(boolean b);
    boolean isEnabled();

    String getStringHash(String password);

    boolean isPlayerLoggedIn(OAPlayer player);
    boolean isPlayerLoggedIn(String player);

    LoginStatus getPlayerStatus(OAPlayer player);

    void processPlayerLogin(OAPlayer player);
    void processPlayerLogout(OAPlayer player);
    boolean processPlayerIdentification(OAPlayer player, String password);
    void processPlayerRegistration(OAPlayer player, String password);

    List<OAPlayer> getActivePlayers();

}