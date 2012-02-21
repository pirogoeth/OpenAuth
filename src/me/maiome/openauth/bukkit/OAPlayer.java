package me.maiome.openauth.bukkit;

// bukkit imports
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.Permission;

public class OAPlayer {
    private final OAServer server;
    private final Player player;
    private final LogHandler log = new LogHandler();
    private PlayerState state;

    private String player_ip = null;

    // PlayerState enum

    private enum PlayerState {
        ONLINE,
        OFFLINE,
        UNKNOWN
    }

    // construct ALL the things!

    public OAPlayer(OAServer server, Player player) {
        this.player = player;
        this.server = server;
        this.state = PlayerState.ONLINE;

        this.player_ip = this.player.getAddress().getAddress().toString();
    }

    // wrapper methods

    public String getName() {
        return this.player.getName();
    }

    public String getIP() {
        return this.player_ip;
    }

    public Player getPlayer() {
        return this.player;
    }

    public OAServer getServer() {
        return this.server;
    }

    public Location getLocation() {
        return this.player.getLocation();
    }

    // movement-type methods

    public double getPitch() {
        return this.player.getLocation().getPitch();
    }

    public double getYaw() {
        return this.player.getLocation().getYaw();
    }

    public void setLocation(Location location) {
        this.player.teleport(location);
    }

    public void setLocation(Location location, double pitch, double yaw) {
        float _pitch = Float.parseFloat(Double.toString(pitch)), _yaw = Float.parseFloat(Double.toString(yaw));
        location.setPitch(_pitch);
        location.setYaw(_yaw);
        this.setLocation(location);
    }

    public void setLocation(Location location, float pitch, float yaw) {
        location.setPitch(pitch);
        location.setYaw(yaw);
        this.setLocation(location);
    }

    // permission methods

    public boolean hasPermission(String node) {
        return Permission.has(this.player, node);
    }

    // state methods

    public void setOffline() {
	    this.state = PlayerState.OFFLINE;
    }

    public void setOnline() {
	    this.state = PlayerState.ONLINE;
    }
}
