package me.maiome.openauth.handlers;

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerLoginEvent;

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
import me.maiome.openauth.bukkit.events.*;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.Permission;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.LoginStatus;

public class OAActiveLoginHandler implements OALoginHandler {

    protected final int factor = (17 * 8);
    protected final int serial = 400;

    private List<OAPlayer> active = new ArrayList<OAPlayer>();
    private OpenAuth controller;
    private LogHandler log = new LogHandler();
    protected boolean enabled = false;

    public OAActiveLoginHandler(OpenAuth controller) {
        this.controller = controller;
    }

    public String toString() {
        return String.format("OAActiveLoginHandler{enabled=%b}", this.enabled);
    }

    public int hashCode() {
        return (int) Math.abs(((this.factor) + (this.controller.hashCode() + this.serial)));
    }

    public void setEnabled(boolean b) {
        this.enabled = b;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public String getStringHash(String password) {
        if (!(this.isEnabled())) return null;
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
        if (!(this.isEnabled())) return true;
        return this.active.contains(this.controller.wrap(this.controller.getServer().getPlayer(player)));
    }

    public boolean isRegistered(OAPlayer player) {
        return this.isRegistered(player.getName());
    }

    public boolean isRegistered(String player) {
        return ConfigInventory.DATA.getConfig().isSet("credentials." + player);
    }

    public LoginStatus getPlayerStatus(OAPlayer player) {
        return (this.isPlayerLoggedIn(player) == true) ? LoginStatus.ACTIVE : LoginStatus.INACTIVE;
    }

    public void processPlayerLogin(PlayerLoginEvent event, OAPlayer player) {
        if (!(this.isEnabled())) return;
        OAPlayerLoginEvent _event = new OAPlayerLoginEvent(player);
        this.controller.getOAServer().callEvent(_event);
        player.setOnline();
        // fix the players location.
        player.fixLocation();
        if (this.controller.getOAServer().hasNameBan(player.getName())) {
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, this.controller.getOAServer().getNameBanReason(player.getName()));
            return;
        } else {
            this.log.exDebug(String.format("%s matched no name bans!", player.getName()));
        }
        if (this.controller.getOAServer().hasIPBan(player.getIP())) {
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, this.controller.getOAServer().getIPBanReason(player.getIP()));
            return;
        } else {
            this.log.exDebug(String.format("%s (%s) matched no IP bans!", event.getAddress().toString(), player.getName()));
        }
        event.allow();
        // check ip stuff.
        if (player.hasIPChanged()) {
            // this user may not be trusted
            player.getSession().setIdentified(false, true);
        }
        return;
    }

    public void processPlayerLogout(OAPlayer player) {
        if (!(this.isEnabled())) return;
        player.setOffline();
    }

    public boolean processPlayerIdentification(OAPlayer player, String password) {
        if (!(this.isEnabled())) {
            player.sendMessage(ChatColor.BLUE + "Authentication is not enabled. You're in the clear.");
            return true;
        }
        String match = ConfigInventory.DATA.getConfig().getString(String.format("credentials.%s.password", player.getName()));
        return ((this.getStringHash(password)).equals(match)) ? true : false;
    }

    public void processPlayerRegistration(OAPlayer player, String password) {
        if (!(this.isEnabled())) return;
        ConfigInventory.DATA.getConfig().set(String.format("credentials.%s.password", player.getName()), this.getStringHash(password));
        OAPlayerRegistrationEvent event = new OAPlayerRegistrationEvent(player);
        OpenAuth.getOAServer().callEvent(event);
    }

    public boolean compareToCurrent(OAPlayer player, String password) {
        if (!(this.isEnabled())) return false;
        return (ConfigInventory.DATA.getConfig().getString(String.format("credentials.%s.password"))).equals(this.getStringHash(password));
    }

    public List<OAPlayer> getActivePlayers() {
        return this.active;
    }
}