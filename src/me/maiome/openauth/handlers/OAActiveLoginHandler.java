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
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.bukkit.events.*;
import me.maiome.openauth.security.*;
import me.maiome.openauth.database.DBPlayer;
import me.maiome.openauth.util.*;

public class OAActiveLoginHandler implements OALoginHandler {

    private List<OAPlayer> active = new ArrayList<OAPlayer>();
    private final OpenAuth controller;
    private LogHandler log = new LogHandler();
    protected boolean enabled = false;

    public OAActiveLoginHandler() {
        this.controller = OpenAuth.getInstance();
    }

    public String toString() {
        return String.format("OAActiveLoginHandler{enabled=%b}", this.enabled);
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
        return this.active.contains(OAPlayer.getPlayer(player));
    }

    public boolean isRegistered(OAPlayer player) {
        return this.isRegistered(player.getName());
    }

    public boolean isRegistered(String player) {
        boolean exists = false;
        try {
            exists = (OpenAuth.getInstance().getDatabase().find(DBPlayer.class, player).getPassword() != null);
        } catch (java.lang.NullPointerException e) {
        } finally {
            return exists;
        }
    }

    public LoginStatus getPlayerStatus(OAPlayer player) {
        return (this.isPlayerLoggedIn(player) == true) ? LoginStatus.ACTIVE : LoginStatus.INACTIVE;
    }

    public void processPlayerLogin(PlayerLoginEvent event, OAPlayer player) {
        if (!(this.isEnabled())) return;
        OAPlayerLoginEvent _event = new OAPlayerLoginEvent(player);
        OAServer.getInstance().callEvent(_event);
        if (_event.isCancelled()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, _event.getCancelReason());
            return;
        }
        player.setOnline();
        if (OAServer.getInstance().hasNameBan(player.getName())) {
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, OAServer.getInstance().getNameBanReason(player.getName()));
            return;
        } else {
            this.log.debug(String.format("%s matched no name bans!", player.getName()));
        }
        if (OAServer.getInstance().hasIPBan(player.getIP())) {
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, OAServer.getInstance().getIPBanReason(player.getIP()));
            return;
        } else {
            this.log.debug(String.format("%s (%s) matched no IP bans!", player.getIP(), player.getName()));
        }
        event.allow();
        this.active.add(player);
        return;
    }

    public void processPlayerLogout(OAPlayer player) {
        if (!(this.isEnabled())) return;
        this.active.remove(player);
        player.setOffline();
    }

    public boolean processPlayerIdentification(OAPlayer player, String password) {
        if (!(this.isEnabled())) {
            player.sendMessage(ChatColor.BLUE + "Authentication is not enabled. You're in the clear.");
            return true;
        }
        String match = OpenAuth.getInstance().getDatabase().find(DBPlayer.class, player.getName()).getPassword();
        boolean success = ((this.getStringHash(password)).equals(match)) ? true : false;
        if (player.getSession().hasHiddenInventory() && success) {
            player.getSession().unhideInventory();
        }
        OAPlayerAttemptedLoginEvent e = new OAPlayerAttemptedLoginEvent(player, success);
        OAServer.getInstance().callEvent(e);
        return success;
    }

    public boolean processDirectIdentification(OAPlayer player, String password) {
        if (!(this.isEnabled())) {
            return true;
        }
        String match = OpenAuth.getInstance().getDatabase().find(DBPlayer.class, player.getName()).getPassword();
        boolean success = ((this.getStringHash(password)).equals(match)) ? true : false;
        OAPlayerAttemptedLoginEvent e = new OAPlayerAttemptedLoginEvent(player, success);
        OAServer.getInstance().callEvent(e);
        return success;
    }

    public void processPlayerRegistration(OAPlayer player, String password) {
        this.processPlayerRegistration(player.getName(), password);
    }

    public void processPlayerRegistration(String player, String password) {
        if (!(this.isEnabled())) return;
        OAPlayerRegistrationEvent event = new OAPlayerRegistrationEvent(player);
        OAServer.getInstance().callEvent(event);
        DBPlayer data = OpenAuth.getInstance().getDatabase().find(DBPlayer.class, player);
        if (password == null) { // this is a password reset/account removal
            data.setPassword(null);
            data.update();
            return;
        }
        if (OAPasswordSecurity.getActiveSecurityValidator().validate(password) == false) {
            OAPlayer.getPlayer(player).sendMessage(ChatColor.RED + "Your registration was cancelled because " + OAPasswordSecurity.getActiveSecurityValidator().explain());
            return;
        }
        data.setPassword(this.getStringHash(password));
        data.update();
        OAPlayer.getPlayer(player).sendMessage(ChatColor.BLUE + "You have been registered and logged in as '" + player + "'.");
    }

    public boolean compareToCurrent(OAPlayer player, String password) {
        if (!(this.isEnabled())) return false;
        return (OpenAuth.getInstance().getDatabase().find(DBPlayer.class, player.getName()).getPassword()).equals(this.getStringHash(password));
    }

    public List<OAPlayer> getActivePlayers() {
        return this.active;
    }
}
