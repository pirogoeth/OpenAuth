package me.maiome.openauth.database;

// internal
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OpenAuth;

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
    @ManyToOne(cascade = CascadeType.ALL)
    private DBPlayer player;

    /**
     * Location of the point.
     */
    @NotNull
    private Location loc;

    /**
     * Mandatory constructor for ebean use.
     */
    public DBPoint() { }

    /**
     * Main constructor to set up the point values.
     */
    public DBPoint(final OAPlayer player, final String name, final Location loc) {
        this.setPlayer(OpenAuth.getOAServer().getController().getDatabase().find(DBPlayer.class, player.getName()));
        this.setName(name);
        this.setLocation(loc);
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
        return this.loc;
    }

    public void setLocation(final Location loc) {
        this.loc = loc;
    }
}