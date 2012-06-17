package me.maiome.openauth.database;

// internal
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.util.LogHandler;

// bukkit
import org.bukkit.Location;
import org.bukkit.World;

// javax Persistence
import javax.persistence.*;

// ebean validation
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;

/**
 * This represents a point that a user has added with /oap add <pointname>
 */
@Entity
@Table(name = "points")
public class DBPoint {
    /**
     * Name of the point, also primary id.
     */
    @Id
    @NotEmpty
    @NotNull
    private String name;

    /**
     * Owner of the point.
     */
    @ManyToOne(cascade = CascadeType.PERSIST)
    private DBPlayer player;

    /**
     * Location of the point.
     */
    @NotNull private String world;
    @NotNull private double x, y, z;
    @NotNull private float pitch, yaw;

    /**
     * Boolean to mark for deletion.
     */
    @Transient private boolean delete = false;

    /**
     * Mandatory constructor for ebean use.
     */
    public DBPoint() { }

    /**
     * Main constructor to set up the point values.
     */
    public DBPoint(final DBPlayer player, final String name, final Location loc) {
        this.setPlayer(player);
        this.setName(name);
        this.setX(loc.getX());
        this.setY(loc.getY());
        this.setZ(loc.getZ());
        this.setPitch(loc.getPitch());
        this.setYaw(loc.getYaw());
        this.setWorld(loc.getWorld().getName());
    }

    @Transient
    public String toString() {
        return String.format("DBPoint{name=%s,location=%s}", this.name, this.getLocation().toString());
    }

    @Transient
    public boolean equals(Object o) {
        if (!(o instanceof DBPoint)) {
            return false;
        }
        DBPoint point = (DBPoint) o;
        return ((this.name.equals(point.getName())) && (this.getLocation().equals(point.getLocation())));
    }

    @Transient
    public void save() {
        synchronized (OpenAuth.databaseLock) {
            OpenAuth.getInstance().getDatabase().save(this);
        }
    }

    @Transient
    private void delete() {
        synchronized (OpenAuth.databaseLock) {
            OpenAuth.getInstance().getDatabase().delete(this);
        }
    }

    @Transient
    public void update() {
        if (this.markedForDeletion()) {
            this.delete();
            return;
        }
        synchronized (OpenAuth.databaseLock) {
            OpenAuth.getInstance().getDatabase().update(this);
        }
    }

    @Transient
    public void rename(final String name) {
        this.setName(name);
        this.update();
    }

    @Transient
    public void markForDeletion(final boolean b) {
        this.delete = b;
    }

    @Transient
    public boolean markedForDeletion() {
        return this.delete;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public DBPlayer getPlayer() {
        return this.player;
    }

    public void setPlayer(final DBPlayer player) {
        this.player = player;
    }

    public double getX() {
        return this.x;
    }

    public void setX(final double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(final double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(final double z) {
        this.z = z;
    }
    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(final float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(final float yaw) {
        this.yaw = yaw;
    }

    public String getWorld() {
        return this.world;
    }

    public void setWorld(final String world) {
        this.world = world;
    }

    @Transient
    public Location getLocation() {
        World world = OpenAuth.getInstance().getServer().getWorld(this.getWorld());
        if (world == null) {
            return null;
        }
        return new Location(world, this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
    }
}