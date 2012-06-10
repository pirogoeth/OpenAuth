package me.maiome.openauth.database;

// internal
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.util.LogHandler;

// bukkit
import org.bukkit.Location;

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
    @NotNull
    private Location location;

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
        this.setLocation(loc);
        this.save();
    }

    @Transient
    public String toString() {
        return String.format("DBPoint{name=%s,location=%s}", this.name, this.location.toString());
    }

    public void save() {
        OpenAuth.getInstance().getDatabase().save(this);
    }

    public void delete() {
        OpenAuth.getInstance().getDatabase().delete(this);
    }

    public void rename(final String name) {
        this.delete();
        this.setName(name);
        this.save();
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

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(final Location loc) {
        this.location = loc;
    }
}