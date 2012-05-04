package me.maiome.openauth.bukkit;

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;

// java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.Permission;
import me.maiome.openauth.util.LoginStatus;
import me.maiome.openauth.util.WhitelistStatus;

public class OAPlayer {

    private final OAServer server;
    private final Player player;
    private final LogHandler log = new LogHandler();
    private Session session = null;
    private final SessionController sc;
    private List<String> ip_list = new ArrayList<String>();
    private Map<String, Object> locations = new HashMap<String, Object>();
    private PlayerState state;
    private boolean ip_changed = false;
    private boolean flying = false;

    private String player_ip = null;

    // construct ALL the things!

    public OAPlayer(Player player) {
        this.player = player;
        this.server = OpenAuth.getOAServer();
        this.state = PlayerState.UNKNOWN;
        this.sc = OpenAuth.getSessionController();

        this.player_ip = player.getAddress().getAddress().toString();
    }

    public OAPlayer(PlayerLoginEvent event) {
        this.player = event.getPlayer();
        this.server = OpenAuth.getOAServer();
        this.state = PlayerState.UNKNOWN;
        this.sc = OpenAuth.getSessionController();

        this.player_ip = event.getAddress().toString();
    }

    // enumerate all possible player states

    private enum PlayerState {
        ONLINE,
        OFFLINE,
        BANNED,
        UNKNOWN
    }

    // enumerate all directions a player could be facing.

    public enum Direction {
        NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST;
    }

    // wrapper methods

    public String getName() {
        return this.player.getName();
    }

    public Player getPlayer() {
        return this.player;
    }

    public OAServer getServer() {
        return this.server;
    }

    public int getItemInHand() {
        return this.player.getItemInHand().getTypeId();
    }

    public void sendMessage(String message) {
        this.player.sendMessage(message);
    }

    public void kickPlayer(String reason) {
        this.player.kickPlayer(reason);
        this.setOffline();
    }

    // IP methods
    public String getIP() {
        return this.player_ip;
    }

    public boolean hasIPChanged() {
        return this.ip_changed;
    }

    public void updateIP() {
        try {
            this.player_ip = (this.player.getAddress().toString() == this.player_ip) ? this.player_ip : this.player.getAddress().toString();
            if (!(this.ip_list.contains(this.player_ip))) {
                this.ip_list.add(this.player_ip);
                this.ip_changed = true;
            } else {
                this.ip_changed = false;
            }
        } catch (java.lang.NullPointerException e) {
            // can't really do anything about this, sadly.
            this.ip_changed = false;
        }
    }

    // conv/support methods

    public void fly(boolean f) {
        this.player.setAllowFlight(true);
        if (f) {
            this.player.setFlying(true);
        } else if (!(f)) {
            this.player.setFlying(false);
        }
    }

    // this is called whenever the player moves.
    public void moved() {
        if (this.getSession().isFrozen()
            && ConfigInventory.MAIN.getConfig().getBoolean("auth.freeze-actions.movement", true) == true
            && !(this.getSession().isIdentified())) {

            this.sendMessage(ChatColor.RED + "You must first identify to move.");
            this.setLocation(this.getLocation());
        } else if (this.getSession().isFrozen() && this.getSession().isIdentified()) {
            this.sendMessage(ChatColor.RED + "You have been frozen.");
            this.setLocation(this.getLocation());
        }
        this.flying = this.getPlayer().isFlying();
    }

    // movement/location-type methods

    public void fixLocation() {
        if (this.getLocation().getWorld().getBlockAt(this.getLocation()).getTypeId() != 0) {
            Location loc = this.getLocation();
            int block_id = 1;
            while (block_id != 0) {
                loc = new Location(loc.getWorld(), loc.getX(), loc.getY() + 1, loc.getZ());
                block_id = loc.getWorld().getBlockAt(loc).getTypeId();
            }
            this.setLocation(loc);
        }
        return;
    }

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
        this.setLocation(location, _pitch, _yaw);
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
        this.setLocation(this.getWorld(), x, y, z, pitch, yaw);
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

    // location serialisation

    public void saveLocation(String name) {
        this.saveLocation(name, this.getLocation());
    }

    public void saveLocation(String name, Location loc) {
        this.locations.put(name, loc);
    }

    public Location getSavedLocation(String name) {
        return (Location) this.locations.get(name);
    }

    public Map<String, Object> getSavedLocations() {
        return this.locations;
    }

    public boolean hasSavedLocation(String name) {
        return this.locations.containsKey(name);
    }

    public void deleteLocation(String name) {
        this.locations.remove(name);
    }

    /**
     * Returns the direction the player is currently facing, split into eight possible directions.
     */
    public Direction getDirection() {
        double rot = (this.getYaw() - 90) % 360;
        if (rot < 0) rot += 360.0;
        if (0 <= rot && rot < 22.5) return Direction.NORTH; // +z
        else if (22.5 <= rot && rot < 67.5) return Direction.NORTH_EAST;
        else if (67.5 <= rot && rot < 112.5) return Direction.EAST; // -x
        else if (112.5 <= rot && rot < 157.5) return Direction.SOUTH_EAST;
        else if (157.5 <= rot && rot < 202.5) return Direction.SOUTH; // -z
        else if (202.5 <= rot && rot < 247.5) return Direction.SOUTH_WEST;
        else if (247.5 <= rot && rot < 292.5) return Direction.WEST; // +x
        else if (292.5 <= rot && rot < 337.5) return Direction.NORTH_WEST;
        else if (337.5 <= rot && rot < 360.0) return Direction.NORTH; // +z
        else return null;
    }

    /**
     * Returns the direction the player is currently facing, simplified into the four basic cardinals.
     */
    public Direction getSimpleDirection() {
        switch (this.getDirection()) {
            case NORTH:
                return Direction.NORTH;
            case NORTH_EAST:
                return Direction.NORTH;
            case EAST:
                return Direction.EAST;
            case SOUTH_EAST:
                return Direction.SOUTH;
            case SOUTH:
                return Direction.SOUTH;
            case SOUTH_WEST:
                return Direction.SOUTH;
            case WEST:
                return Direction.WEST;
            case NORTH_WEST:
                return Direction.NORTH;
            default:
                return null;
        }
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
        if (this.session != null) this.updateIP();
        if (this.flying) this.fly(true);
        this.state = PlayerState.ONLINE;
        try {
            this.locations = (Map<String, Object>) ConfigInventory.DATA.getConfig().getConfigurationSection(String.format("locations.%s", this.getName())).getValues(true);
        } catch (java.lang.NullPointerException e) {
            this.locations = new HashMap<String, Object>();
        }
        // this.getServer().callEvent(new OAPlayerOnlineEvent(this));
    }

    public boolean isOnline() {
        return (this.state == PlayerState.ONLINE) ? true : false;
    }

    public void setOffline() {
        try {
            this.session.clearAction();
        } catch (java.lang.NullPointerException e) {}
        this.setState(PlayerState.OFFLINE);
        ConfigInventory.DATA.getConfig().set(String.format("locations.%s", this.getName()), this.locations);
        ConfigInventory.DATA.save();
        // this.getServer().callEvent(new OAPlayerOfflineEvent(this));
    }

    private void setState(PlayerState state) {
        this.state = state;
        // this.getServer().callEvent(new OAPlayerStateChangedEvent(this, state));
    }

    public PlayerState getState() {
        return this.state;
    }

    // session methods

    public void initSession() {
        this.session = this.sc.get(this);
    }

    public Session getSession() {
        if (this.session == null && this.state == PlayerState.ONLINE) {
            this.initSession();
            log.exDebug(String.format("Forced a session for online player %s.", this.getName()));
        } else if (this.session != this.sc.get(this) && this.session != null) {
            this.initSession();
            log.exDebug(String.format("Resetting session for player %s.", this.getName()));
        }
        return this.session;
    }

    public void destroySession() {
        this.sc.forget(this);
    }

    // other methods

    public WhitelistStatus isWhitelisted() {
        return this.server.getPlayerWhitelistStatus(this);
    }

    public LoginStatus isPlayerIdentified() {
        return this.server.getPlayerLoginStatus(this);
    }
}
