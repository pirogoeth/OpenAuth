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
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.util.Permission;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;

public class OAServer {
    public Server server;
    public OpenAuth plugin;
    private final LogHandler log = new LogHandler();

    private Map<String, String> ip_bans = new HashMap<String, String>();
    private Map<String, String> name_bans = new HashMap<String, String>();

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

    public Server getServer() {
        return this.server;
    }

    // oa methods

    public void kickPlayer(final OAPlayer player) {
        player.getPlayer().kickPlayer("No reason.");
        log.info("Kicked player: " + player.getName());
        player.setOffline();
    }

    public void kickPlayer(final OAPlayer player, final String reason) {
        player.getPlayer().kickPlayer(reason);
        log.info("Kicked player: " + player.getName() + ", reason: " + reason);
        player.setOffline();
    }

    public void banPlayerByIP(final OAPlayer player) {
        if (!(this.ip_bans.containsKey(player.getIP()))) {
            this.ip_bans.put(player.getIP(), null);
        }
    }

    public void banPlayerByIP(final OAPlayer player, final String reason) {
        if (!(this.ip_bans.containsKey(player.getIP()))) {
            this.ip_bans.put(player.getIP(), reason);
        }
    }

    public void banPlayerByName(final OAPlayer player) {
        if (!(this.name_bans.containsKey(player.getName()))) {
            this.name_bans.put(player.getName(), null);
        }
    }

    public void banPlayerByName(final OAPlayer player, final String reason) {
        if (!(this.name_bans.containsKey(player.getName()))) {
            this.name_bans.put(player.getName(), reason);
        }
    }
}

