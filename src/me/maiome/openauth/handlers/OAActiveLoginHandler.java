package me.maiome.openauth.handlers;

// java imports
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.security.*;
import java.math.*;

// internal imports
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.Permission;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.LoginStatus;

public class OAActiveLoginHandler implements OALoginHandler {

    private List<OAPlayer> active = new ArrayList<OAPlayer>();
    private OpenAuth controller;
    private LogHandler log = new LogHandler();

    public OAActiveLoginHandler(OpenAuth controller) {
        this.controller = controller;
    }

    public String getStringHash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes(), 0, password.length());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isPlayerLoggedIn(OAPlayer player) {
        return this.active.contains(player);
    }

    public boolean isPlayerLoggedIn(String player) {
        return this.active.contains(this.controller.wrapOAPlayer(this.controller.getServer().getPlayer(player)));
    }

    public LoginStatus getPlayerStatus(OAPlayer player) {
        return (this.isPlayerLoggedIn(player) == true) ? LoginStatus.ACTIVE : LoginStatus.INACTIVE;
    }

    public void processPlayerLogin(OAPlayer player) {
        player.setOnline();
        if (this.controller.getOAServer().hasNameBan(player.getName())) {
            this.controller.getOAServer().kickPlayer(
                player,
                this.controller.getOAServer().getNameBanReason(player.getName()));
            return;
        } else {
            this.log.exDebug(String.format("%s successfully matched no name bans!", player.getName()));
        }
        if (this.controller.getOAServer().hasIPBan(player.getIP())) {
            this.controller.getOAServer().kickPlayer(
                player,
                this.controller.getOAServer().getIPBanReason(player.getIP()));
            return;
        } else {
            this.log.exDebug(String.format("%s(%s) successfully matched no IP bans!", player.getIP(), player.getName()));
        }
        return;
    }

    public void processPlayerLogout(OAPlayer player) {
        player.setOffline();
    }

    public boolean processPlayerIdentification(OAPlayer player, String password) {
        String match = ConfigInventory.DATA.getConfig().getString(String.format("credentials.%s.password"));
        return ((this.getStringHash(password)).equals(match)) ? true : false;
    }

    public List<OAPlayer> getActivePlayers() {
        return this.active;
    }
}