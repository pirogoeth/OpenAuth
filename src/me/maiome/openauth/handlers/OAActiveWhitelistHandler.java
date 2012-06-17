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

public class OAActiveWhitelistHandler implements OAWhitelistHandler {

    protected final int factor = (17 * 8);
    protected final int serial = 401;

    private OpenAuth controller;
    private List<String> whitelist = new ArrayList<String>();
    private final LogHandler log = new LogHandler();
    protected boolean enabled = false;

    public OAActiveWhitelistHandler(OpenAuth controller) {
        this.controller = controller;
        this.loadWhitelist();
        if (ConfigInventory.MAIN.getConfig().getBoolean("whitelisting.print-on-load", true) == true) {
            log.exDebug("Whitelist:");
            for (String name : this.whitelist) {
                log.exDebug(" => " + name);
            }
        }
    }

    public String toString() {
        return String.format("OAActiveWhitelistHandler{enabled=%b}", this.enabled);
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
        if (!(this.isEnabled())) return;
        if (!(this.isWhitelisted(player))) {
            if (ConfigInventory.MAIN.getConfig().getBoolean("whitelisting.broadcast-failures", false) == true) {
                this.controller.getOAServer().getServer().broadcastMessage(ChatColor.GREEN + String.format(
                    "Player %s has tried to join, but is not whitelisted!", player.getName()));
            }
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "You are not whitelisted on this server!");
            return;
        }
        event.allow();
    }

    public void loadWhitelist() {
        synchronized (OpenAuth.databaseLock) {
            try {
                List<DBWhitelist> whitelist = OpenAuth.getInstance().getDatabase().find(DBWhitelist.class).where("whitelisted == true").findList();
                for (DBWhitelist entry : whitelist) {
                    if (entry.getWhitelisted()) {
                        this.whitelist.add(entry.getName());
                    }
                }
            } catch (java.lang.RuntimeException e) {
                // most likely, this is because the whitelist is EMPTY.
                log.info("Whitelist is most likely empty!");
            }
        }
    }

    public void saveWhitelist() { }; // stub to complete implementation

    public void whitelistPlayer(OAPlayer player) {
        this.whitelistPlayer(player.getName());
    }

    public void whitelistPlayer(Player player) {
        this.whitelistPlayer(player.getName());
    }

    public void whitelistPlayer(String name) {
        if (!(this.isEnabled())) return;
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
        if (!(this.isEnabled())) return;
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