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
import me.maiome.openauth.util.LoginStatus;
import me.maiome.openauth.util.WhitelistStatus;

public class OAPlayer {
    private final OAServer server;
    private final Player player;
    private final LogHandler log = new LogHandler();
    private PlayerState state;

    private String player_ip = null;

    // construct ALL the things!

    public OAPlayer(OAServer server, Player player) {
        this.player = player;
        this.server = server;
        this.state = PlayerState.UNKNOWN;

        this.player_ip = this.player.getAddress().getAddress().toString();
    }

    // enumerate all possible player states

    private enum PlayerState {
        ONLINE,
        OFFLINE,
        BANNED,
        UNKNOWN
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

    // movement-type methods

    public float getPitch() {
        return this.player.getLocation().getPitch();
    }

    public float getYaw() {
        return this.player.getLocation().getYaw();
    }

    public Location getLocation() {
        return this.player.getLocation();
    }

    public World getWorld() {
        return this.player.getWorld();
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

    public void setLocation(float x, float y, float z) {
        Location location = new Location(this.getWorld(), x, y, z);
        location.setPitch(this.getPitch());
        location.setYaw(this.getYaw());
        this.setLocation(location);
    }

    public void setLocation(float x, float y, float z, double pitch, double yaw) {
        float _pitch = Float.parseFloat(Double.toString(pitch)), _yaw = Float.parseFloat(Double.toString(yaw));
        this.setLocation(x, y, z, _pitch, _yaw);
    }

    public void setLocation(float x, float y, float z, float pitch, float yaw) {
        Location location = new Location(this.getWorld(), x, y, z);
        location.setPitch(pitch);
        location.setYaw(yaw);
        this.setLocation(location);
    }

    public void setLocation(World w, float x, float y, float z) {
        Location location = new Location(w, x, y, z);
        this.setLocation(location);
    }

    public void setLocation(World w, float x, float y, float z, float pitch, float yaw) {
        Location location = new Location(w, x, y, z);
        location.setPitch(pitch);
        location.setYaw(yaw);
        this.setLocation(location);
    }

    // permission methods

    public boolean hasPermission(String node) {
        return Permission.has(this.player, node);
    }

    public boolean hasPermission(String node, boolean def) {
        return Permission.has(this.player, node, def);
    }

    // state methods

    public void setOnline() {
        this.state = PlayerState.ONLINE;
    }

    public void setOffline() {
        this.state = PlayerState.OFFLINE;
    }

    private void setState(PlayerState state) {
        this.state = state;
    }

    public PlayerState getState() {
        return this.state;
    }

    // other methods

    public WhitelistStatus isWhitelisted() {
        return this.server.getPlayerWhitelistStatus(this);
    }

    public LoginStatus isPlayerIdentified() {
        return this.server.getPlayerLoginStatus(this);
    }
}
