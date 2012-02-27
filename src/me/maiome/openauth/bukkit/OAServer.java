package me.maiome.openauth.bukkit;

// java imports
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

// bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;

// internal imports
import me.maiome.openauth.OServer;
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.util.Permission;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;

public class OAServer implements OServer {
    public Server server;
    public OpenAuth plugin;

    public OAServer(OpenAuth plugin, Server server) {
        this.plugin = plugin;
        this.server = server;
    }

    // scheduling

    public int scheduleSyncRepetitiveTask(long delay, long period, Runnable task) {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, task, delay, period);
    }

    public int scheduleSyncDelayedTask(long delay, Runnable task) {
        return Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, task, delay);
    }

    public int scheduleAsynchronousRepeatingTask(long delay, long period, Runnable task) {
        return Bukkit.getScheduler().scheduleAsyncRepeatingTask(this.plugin, task, delay, period);
    }

    // support

    @Override
    public Server getServer() {
        return this.server;
    }

    // oa methods

    @Override
    public void kickPlayer(final OAPlayer player) {
        player.getPlayer().kickPlayer("No reason.");
        log.info("Kicked player: " + player.getName());
        player.setOffline();
    }

    @Override
    public void kickPlayer(final OAPlayer player, final String reason) {
        player.getPlayer().kickPlayer(reason);
        log.info("Kicked player: " + player.getName() + ", reason: " + reason);
        player.setOffline();
    }

    @Override
    public void banPlayerByIP(final OAPlayer player) {
        if (!(this.ip_bans.containsKey(player.getIP()))) {
            this.ip_bans.put(player.getIP(), null);
        }
    }

    @Override
    public void banPlayerByIP(final OAPlayer player, final String reason) {
        if (!(this.ip_bans.containsKey(player.getIP()))) {
            this.ip_bans.put(player.getIP(), reason);
        }
    }

    @Override
    public void banPlayerByIP(final String IP) {
        if (!(this.ip_bans.containsKey(IP))) {
            this.ip_bans.put(IP, null);
        }
    }

    @Override
    public void banPlayerByIP(final String IP, final String reason) {
        if (!(this.ip_bans.containsKey(IP))) {
            this.ip_bans.put(IP, reason);
        }
    }

    @Override
    public void unbanPlayerByIP(final OAPlayer player) {
        if (!(this.ip_bans.containsKey(player.getIP()))) {
            this.ip_bans.remove(player.getIP());
        }
    }

    @Override
    public void unbanPlayerByIP(final String IP) {
        if (!(this.ip_bans.containsKey(IP))) {
            this.ip_bans.remove(IP);
        }
    }

    @Override
    public void banPlayerByName(final OAPlayer player) {
        if (!(this.name_bans.containsKey(player.getName()))) {
            this.name_bans.put(player.getName(), null);
        }
    }

    @Override
    public void banPlayerByName(final OAPlayer player, final String reason) {
        if (!(this.name_bans.containsKey(player.getName()))) {
            this.name_bans.put(player.getName(), reason);
        }
    }

    @Override
    public void banPlayerByName(final String player) {
        if (!(this.name_bans.containsKey(player))) {
            this.name_bans.put(player, null);
        }
    }

    @Override
    public void banPlayerByName(final String player, final String reason) {
        if (!(this.name_bans.containsKey(player))) {
            this.name_bans.put(player, reason);
        }
    }

    @Override
    public void unbanPlayerByName(final OAPlayer player) {
        if (!(this.name_bans.containsKey(player.getName()))) {
            this.name_bans.remove(player.getName());
        }
    }

    @Override
    public void unbanPlayerByName(final String player) {
        if (!(this.name_bans.containsKey(player))) {
            this.name_bans.remove(player);
        }
    }

    // whitelisting, login status methods

    public boolean isPlayerWhitelisted(final OAPlayer player) {
        // return this.whitelistHandler.getPlayerStatus(player);
        return true;
    }

    public boolean getLoginStatus(final OAPlayer player) {
        // return this.loginHandler.getPlayerStatus(player);
        return true;
    }
}

