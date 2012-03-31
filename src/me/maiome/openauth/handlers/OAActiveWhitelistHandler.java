package me.maiome.openauth.handlers;

// internal
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.util.*;

// bukkit
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

// java
import java.util.ArrayList;
import java.util.List;

public class OAActiveWhitelistHandler implements OAWhitelistHandler {

    private List<String> whitelist = new ArrayList<String>();
    private OpenAuth controller;
    private final LogHandler log = new LogHandler();
    protected boolean enabled = false;

    public OAActiveWhitelistHandler(OpenAuth controller) {
        this.controller = controller;
        this.loadWhitelist();
        log.exDebug("Whitelist:");
        for (String name : this.whitelist) {
            log.exDebug(" => " + name);
        }
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

    public void processPlayerJoin(OAPlayer player) {
        if (!(this.isEnabled())) return;
        if (!(isWhitelisted(player))) {
            this.controller.getOAServer().kickPlayer(player, "You are not whitelisted on this server!");
            if (ConfigInventory.MAIN.getConfig().getBoolean("whitelisting.broadcast-failures", false) == true) {
                this.controller.getOAServer().getServer().broadcastMessage(ChatColor.GREEN + String.format(
                    "Player %s has tried to join, but is not whitelisted!", player.getName()));
            }
        }
    }

    public void saveWhitelist() {
        ConfigInventory.DATA.getConfig().set("whitelist", this.whitelist);
        log.exDebug("Saved whitelist.");
    }

    public void loadWhitelist() {
        this.whitelist.addAll(ConfigInventory.DATA.getConfig().getStringList("whitelist"));
        log.exDebug("Loaded whitelist.");
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
        } else {
            return;
        }
        return;
    }

    public List<String> getWhitelist() {
        return this.whitelist;
    }
}