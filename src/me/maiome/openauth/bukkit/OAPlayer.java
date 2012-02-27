package me.maiome.openauth.bukkit;

// bukkit imports
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

// internal imports
import me.maiome.openauth.OPlayer;
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.Permission;

public class OAPlayer implements OPlayer {
    private final OAServer server;
    private final Player player;
    private final LogHandler log = new LogHandler();
    private PlayerState state;

    private String player_ip = null;

    // construct ALL the things!

    public OAPlayer(OAServer server, Player player) {
        super(server);
        this.player = player;
        this.server = server;
        this.state = PlayerState.UNKNOWN;

        this.player_ip = this.player.getAddress().getAddress().toString();
    }

    // wrapper methods

    @Override
    public String getName() {
        return this.player.getName();
    }

    @Override
    public String getIP() {
        return this.player_ip;
    }

    public Player getPlayer() {
        return this.player;
    }

    @Override
    public OAServer getServer() {
        return this.server;
    }

    // movement-type methods

    @Override
    public double getPitch() {
        return this.player.getLocation().getPitch();
    }

    @Override
    public double getYaw() {
        return this.player.getLocation().getYaw();
    }

    @Override
    public Location getLocation() {
        return this.player.getLocation();
    }

    @Override
    public void setLocation(Location location) {
        this.player.teleport(location);
    }

    @Override
    public void setLocation(Location location, double pitch, double yaw) {
        float _pitch = Float.parseFloat(Double.toString(pitch)), _yaw = Float.parseFloat(Double.toString(yaw));
        location.setPitch(_pitch);
        location.setYaw(_yaw);
        this.setLocation(location);
    }

    @Override
    public void setLocation(Location location, float pitch, float yaw) {
        location.setPitch(pitch);
        location.setYaw(yaw);
        this.setLocation(location);
    }

    @Override
    public void setLocation(int x, int y, int z) {
        Location location = new Location(x, y ,z);
        location.setPitch(this.getPitch());
        location.setYaw(this.getYaw());
        this.setLocation(location);
    }

    @Override
    public void setLocation(int x, int y, int z, double pitch, double yaw) {
        float _pitch = Float.parseFloat(Double.toString(pitch)), _yaw = Float.parseFloat(Double.toString(yaw));
        this.setLocation(x, y, z, _pitch, _yaw);
    }

    @Override
    public void setLocation(int x, int y, int z, float pitch, float yaw) {
        Location location = new Location(x, y, z);
        location.setPitch(pitch);
        location.setYaw(yaw);
        this.setLocation(location);
    }

    // permission methods

    @Override
    public boolean hasPermission(String node) {
        return Permission.has(this.player, node);
    }

    @Override
    public boolean hasPermission(String node, boolean default) {
        return Permission.has(this.player, node, default);
    }

    // other methods

    public boolean isWhitelisted() {
        return this.server.isPlayerWhitelisted(this);
    }

    public boolean isPlayerIdentified() {
        return this.server.getLoginStatus(this);
    }
}
