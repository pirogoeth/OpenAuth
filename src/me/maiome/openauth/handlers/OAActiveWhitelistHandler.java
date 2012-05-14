package me.maiome.openauth.handlers;

// internal
import me.maiome.openauth.bukkit.*;
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

    private List<String> whitelist = new ArrayList<String>();
    private OpenAuth controller;
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
        if (!(isWhitelisted(player))) {
            if (ConfigInventory.MAIN.getConfig().getBoolean("whitelisting.broadcast-failures", false) == true) {
                this.controller.getOAServer().getServer().broadcastMessage(ChatColor.GREEN + String.format(
                    "Player %s has tried to join, but is not whitelisted!", player.getName()));
            }
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "You are not whitelisted on this server!");
            return;
        }
        event.allow();
    }

    public void saveWhitelist() {
        ConfigInventory.DATA.getConfig().set("whitelist", this.whitelist);
    }

    public void loadWhitelist() {
        this.whitelist.addAll(ConfigInventory.DATA.getConfig().getStringList("whitelist"));
    }

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
            this.saveWhitelist();
        } else {
            return;
        }
        return;
    }

    public List<String> getWhitelist() {
        return this.whitelist;
    }
}