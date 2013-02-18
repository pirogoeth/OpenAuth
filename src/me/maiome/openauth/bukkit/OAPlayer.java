package me.maiome.openauth.bukkit;

// bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

// java imports
import java.lang.Math;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.bukkit.events.*;
import me.maiome.openauth.database.DBPlayer;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.LoginStatus;
import me.maiome.openauth.util.Permission;
import me.maiome.openauth.util.WhitelistStatus;

public class OAPlayer {

    // class internal identification
    protected transient final int factor = (17 * 5);
    protected transient final int serial = 101;

    // static player getter/storage and implementation
    public static final Map<String, OAPlayer> players = new HashMap<String, OAPlayer>();

    private static OAPlayer createPlayer(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Player) {
            Player bplayer = (Player) obj;
            OAPlayer pl = new OAPlayer(bplayer);
            players.put(bplayer.getName(), pl);
            return players.get(bplayer.getName());
        } else if (obj instanceof PlayerLoginEvent) {
            Player bplayer = ((PlayerLoginEvent) obj).getPlayer();
            OAPlayer pl = new OAPlayer((PlayerLoginEvent) obj);
            players.put(bplayer.getName(), pl);
            return players.get(bplayer.getName());
        } else if (obj instanceof String) {
            Player bplayer = OpenAuth.getOAServer().getServer().getPlayer((String) obj);
            players.put(bplayer.getName(), new OAPlayer(bplayer));
            return players.get(bplayer.getName());
        } else {
            return null;
        }
    }

    public static OAPlayer getPlayer(Object obj) {
        if (obj == null) return null;
        OAPlayer player;
        if (obj instanceof Player) {
            String name = ((Player) obj).getName();
            player = players.get(name);
        } else if (obj instanceof PlayerLoginEvent) {
            String name = ((PlayerLoginEvent) obj).getPlayer().getName();
            player = players.get(name);
        } else if (obj instanceof String) {
            String name = (String) obj;
            player = players.get(name);
        } else {
            return null;
        }
        if (player == null && obj != null) {
            player = createPlayer(obj);
            if (player == null) {
                return null;
            }
            return player;
        } else if (player != null) {
            return player;
        }
        return null;
    }

    public static boolean hasPlayer(Object obj) {
        if (obj == null) return false;
        OAPlayer player = null;
        if (obj instanceof Player) {
            String name = ((Player) obj).getName();
            player = players.get(name);
        } else if (obj instanceof PlayerLoginEvent) {
            String name = ((PlayerLoginEvent) obj).getPlayer().getName();
            player = players.get(name);
        } else if (obj instanceof String) {
            String name = (String) obj;
            player = players.get(name);
        } else {
            return false;
        }
        if (player == null && obj != null) {
            return false;
        } else if (player != null) {
            return true;
        }
        return false;
    }

    // normal class variables
    private final OAServer server;
    private final LogHandler log = new LogHandler();
    private final SessionController sc;
    private DBPlayer data = null;
    private Player player;
    private String name = null;
    private Session session = null;
    private PlayerState state = PlayerState.UNKNOWN;
    private boolean flying = false;

    private String player_ip = null;

    // ip update task
    private Runnable updateip = new Runnable() {
        public void run() {
            updateIP();
        }
    };

    // ip comparison task
    public Runnable ip_comparison = new Runnable() {
        public void run() {
            if (session != null) {
                if (session.getIP().trim().equals("") && !(getIP().equals(null))) return; // first login, no IP on record.
                if (!(session.getIP().equals(getIP()))) {
                    log.info(String.format("WARNING! %s may not be who they claim! Their IP has changed from %s to %s, resetting session!", getName(), session.getIP(), getIP()));
                    destroySession(); // destroy the old session
                    initSession(); // get a new session
                } else {
                    if (player_ip != null && !(player_ip.trim().equals(""))) {
                        getSession().setIP(player_ip);
                    }
                }
            }
        }
    };

    // construct ALL the things!

    private OAPlayer(Player player) {
        this.player = player;
        this.name = player.getName();
        synchronized (OpenAuth.databaseLock) {
            this.data = OpenAuth.getInstance().getDatabase().find(DBPlayer.class, this.name);
            if (this.data == null) {
                this.data = new DBPlayer(this.name);
            }
        }
        this.server = OpenAuth.getOAServer();
        this.state = PlayerState.UNKNOWN;
        this.sc = OpenAuth.getSessionController();
        this.session = this.getSession();
    }

    private OAPlayer(PlayerLoginEvent event) {
        this(event.getPlayer());
        this.player_ip = event.getAddress().getHostAddress();
    }

    public String toString() {
        return String.format("OAPlayer{name=%s,ip=%s,state=%s}", this.getName(), this.player_ip, this.state.toString());
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof OAPlayer)) return false;
        if (obj == null) return false;

        OAPlayer pl = null;

        try {
            pl = (OAPlayer) obj;
        } catch (java.lang.ClassCastException e) {
            return false;
        }

        if (pl.getName().equals(this.getName()) && pl.getIP() == this.getIP()) return true;
        else return false;
    }

    // THIS IS SUPER IMPORTANT. basically, updates all the player information and such to keep outdates from happening.

    public void update() {
        Player player = Bukkit.getServer().getPlayerExact(this.name);
        try {
            try {
                if (player == null) {
                    // player most likely logged off?
                } else if (this.player == null) {
                    try {
                        this.player = Bukkit.getServer().getPlayerExact(this.name);
                    } catch (java.lang.Exception e) {
                        this.player = null; // we just broke everything with this line :D
                    }
                } else if (player != null && !(player.equals(this.player))) {
                    this.player = player;
                }
            } catch (java.lang.Exception e) {
                e.printStackTrace();
            }
        } catch (java.lang.NullPointerException e) {
            log.warning("There's a chance things have broken like my Lego Star Wars set. TACTICAL STACKTRACE INBOUND.");
            e.printStackTrace();
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
        return;
    }

    // enumerate all possible player states

    public enum PlayerState {
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
        return (this.name != null ? this.name : (this.player.getName() != null ? this.player.getName() : null));
    }

    public int getEntityId() {
        return this.player.getEntityId();
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

    public void updateIP() {
        try {
            this.player_ip = (this.player.getAddress().getAddress().getHostAddress() == this.player_ip) ? this.player_ip : this.player.getAddress().getAddress().getHostAddress();
        } catch (java.lang.NullPointerException e) {
            this.getServer().scheduleSyncDelayedTask(100L, this.updateip);
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
    public void moved(PlayerMoveEvent event) {
        if (this.getSession().isIdentified() == false
            && ConfigInventory.MAIN.getConfig().getBoolean("auth.freeze-actions.movement", true) == true) {

            this.sendMessage(ChatColor.RED + "You must first identify to move.");
            event.setCancelled(true);
            return;
        } else if (this.getSession().isFrozen() && this.getSession().isIdentified()) {
            this.sendMessage(ChatColor.RED + "You have been frozen.");
            event.setCancelled(true);
            return;
        }
        this.flying = this.getPlayer().isFlying();
    }

    // movement/location-type methods

    public void fixLocation() {
        if (this.getLocation().getWorld().getBlockAt(this.getLocation()).getTypeId() != 0) {
            Location loc = this.getLocation();
            int blockId = -1;
            while (blockId != 0) {
                loc = new Location(loc.getWorld(), loc.getX(), loc.getY() + 1, loc.getZ());
                blockId = loc.getWorld().getBlockAt(loc).getTypeId();
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

    public boolean inCuboid(Location l1, Location l2) {
        Location loc = this.getLocation();
        double x1 = Math.min(l1.getX(), l2.getX()), y1 = Math.min(l1.getY(), l2.getY()), z1 = Math.min(l1.getZ(), l2.getZ());
        double x2 = Math.min(l1.getX(), l2.getX()), y2 = Math.min(l1.getY(), l2.getY()), z2 = Math.min(l1.getZ(), l2.getZ());

        return ((loc.getX() > x1) && (loc.getX() < x2) && (loc.getY() > y1) && (loc.getY() < y2) && (loc.getZ() > z1) && (loc.getZ() < z2));
    }

    public void setLocation(Location location) {
        this.player.teleport(location);
    }

    public void setLocation(Location location, float pitch, float yaw) {
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
        if (this.flying) this.fly(true);
        this.state = PlayerState.ONLINE;
        this.getServer().scheduleSyncDelayedTask(100L, this.updateip);
        this.getServer().scheduleSyncDelayedTask(140L, this.ip_comparison);
        this.getServer().callEvent(new OAPlayerOnlineEvent(this));
    }

    public boolean isOnline() {
        return (this.state == PlayerState.ONLINE) ? true : false;
    }

    public void setOffline() {
        try {
            this.session.clearAction();
        } catch (java.lang.NullPointerException e) {}
        this.setState(PlayerState.OFFLINE);
        this.getServer().callEvent(new OAPlayerOfflineEvent(this));
    }

    private void setState(PlayerState state) {
        this.getServer().callEvent(new OAPlayerStateChangedEvent(this, this.state, state));
        this.state = state;
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
