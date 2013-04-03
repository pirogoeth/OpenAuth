package me.maiome.openauth.handlers;

// internal
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.database.DBWhitelist;
import me.maiome.openauth.util.*;

// bukkit
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;

// java
import java.util.ArrayList;
import java.util.List;

public class OAActiveWhitelistHandler extends Reloadable implements OAWhitelistHandler {

    private OpenAuth controller;
    private List<String> whitelist = new ArrayList<String>();
    private final LogHandler log = new LogHandler();
    protected boolean enabled = false;

    private String denialMessage;
    private boolean broadcastFailures;

    public OAActiveWhitelistHandler() {
        this.reload();
        this.controller = OpenAuth.getInstance();
        this.loadWhitelist();
        this.setReloadable(this);
    }

    protected void reload() {
        this.denialMessage = Config.getConfig().getString("whitelisting.denial-message", "You are not whitelisted on this server!");
        this.broadcastFailures = Config.getConfig().getBoolean("whitelisting.broadcast-failures", false);
    }

    public String toString() {
        return String.format("OAActiveWhitelistHandler{enabled=%b}", this.enabled);
    }

    public void setEnabled(boolean b) {
        this.enabled = b;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isWhitelisted(OAPlayer player) {
        return this.isWhitelisted(player.getName());
    }

    public boolean isWhitelisted(Player player) {
        return this.isWhitelisted(player.getName());
    }

    public boolean isWhitelisted(String name) {
        if (!(this.isEnabled())) return true;
        return this.whitelist.contains(name);
    }

    public WhitelistStatus getPlayerStatus(OAPlayer player) {
        return (this.isWhitelisted(player) == true) ? WhitelistStatus.ALLOWED : WhitelistStatus.DISALLOWED;
    }

    public void processPlayerJoin(PlayerLoginEvent event, OAPlayer player) {
        if (this.isEnabled() == false) return;
        if (!(this.isWhitelisted(player))) {
            if (this.broadcastFailures) {
                OAServer.getInstance().getServer().broadcastMessage(ChatColor.GREEN + String.format(
                    "Player %s has tried to join the server, but is not whitelisted!", player.getName()));
            }
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, this.denialMessage);
            return;
        }
        event.allow();
    }

    public void loadWhitelist() {
        synchronized (OpenAuth.databaseLock) {
            try {
                List<DBWhitelist> whitelist = OpenAuth.getInstance().getDatabase().find(DBWhitelist.class).findList();
                for (DBWhitelist entry : whitelist) {
                    if (entry.getWhitelisted()) {
                        this.whitelist.add(entry.getName());
                    }
                }
            } catch (java.lang.Exception e) {
                e.printStackTrace();
                // most likely, this is because the whitelist is EMPTY.
                log.info("Whitelist is most likely empty!");
            }
        }
    }

    public void saveWhitelist() {
        synchronized (OpenAuth.databaseLock) {
            try {
                for (String entry : this.whitelist) {
                    DBWhitelist wl = OpenAuth.getInstance().getDatabase().find(DBWhitelist.class, entry);
                    wl.setWhitelisted(true, true);
                }
            } catch (java.lang.Exception e) {
                // just say nothing -_-
            }
        }
    }

    public void whitelistPlayer(OAPlayer player) {
        this.whitelistPlayer(player.getName());
    }

    public void whitelistPlayer(Player player) {
        this.whitelistPlayer(player.getName());
    }

    public void whitelistPlayer(String name) {
        if (this.isEnabled() == false) return;
        if (!(this.whitelist.contains(name))) {
            this.whitelist.add(name);
            DBWhitelist entry = OpenAuth.getInstance().getDatabase().find(DBWhitelist.class, name);
            if (entry == null) {
                entry = new DBWhitelist(name);
            }
            entry.setWhitelisted(true, true); // allow and force an update
        } else {
            return;
        }
        return;
    }

    public void unwhitelistPlayer(OAPlayer player) {
        this.unwhitelistPlayer(player.getName());
    }

    public void unwhitelistPlayer(Player player) {
        this.unwhitelistPlayer(player.getName());
    }

    public void unwhitelistPlayer(String name) {
        if (this.isEnabled() == false) return;
        if (this.whitelist.contains(name)) {
            this.whitelist.remove(name);
            DBWhitelist entry = OpenAuth.getInstance().getDatabase().find(DBWhitelist.class, name);
            if (entry == null) {
                entry = new DBWhitelist(name);
            }
            entry.setWhitelisted(false, true); // deny and force an update
        } else {
            return;
        }
        return;
    }

    public List<String> getWhitelist() {
        return this.whitelist;
    }
}
