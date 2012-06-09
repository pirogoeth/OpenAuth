package me.maiome.openauth.database;

// internal
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OpenAuth;

// bukkit
import org.bukkit.Location;

// java
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// javax Persistence
import javax.persistence.*;

// ebean validation
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;

/**
 * This represents a small portion of user settings and properties.
 */
@Entity
@Table(name = "users")
public class DBPlayer {
    /**
     * Name of the point, also primary id.
     */
    @Id
    @NotEmpty
    @NotNull
    private String name;

    /**
     * List of points this player owns.
     */
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<DBPoint> points = new ArrayList<DBPoint>();

    /**
     * List of IP addresses this user has ever connected from.
     */
    @NotEmpty
    @NotNull
    private List<String> addresses = new ArrayList<String>();

    /**
     * OAPlayer transient for use later.
     */
    @Transient
    private OAPlayer player;

    /**
     * Mandatory constructor for ebean use.
     */
    public DBPlayer() { }

    /**
     * Main constructor to set up the point values.
     */
    public DBPlayer(final OAPlayer player) {
        this.player = player;
        this.setName(player.getName());
        // this.setIPList(player.getIPList());
        this.setIP(player.getIP());
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getIP() {
        return this.addresses.get(0);
    }

    public void setIP(final String ip) {
        this.addresses.add(0, ip);
    }

    public List<String> getIPList() {
        return this.addresses;
    }

    public void setIPList(final List<String> iplist) {
        this.points.addAll((Collection) iplist);
    }

    public List<DBPoint> getPoints() {
        return this.points;
    }

    public void setPoints(final List<DBPoint> points) {
        this.points = points;
    }

    public Map<String, Location> getPointList() {
        Map<String, Location> pointmap = new HashMap<String, Location>();
        for (DBPoint point : this.getPoints()) {
            pointmap.put(point.getName(), point.getLocation());
        }
        return pointmap;
    }

    public void setPointList(final Map<String, Location> pointmap) {
        List<DBPoint> updated = new ArrayList<DBPoint>();
        for (Map.Entry<String, Location> es : pointmap.entrySet()) {
            DBPoint point = ((OpenAuth.getOAServer().getController().getDatabase().find(DBPoint.class, es.getKey()) == null) ? new DBPoint(this.player, es.getKey(), es.getValue()) : OpenAuth.getOAServer().getController().getDatabase().find(DBPoint.class, es.getKey()));
            updated.add(point);
        }
        this.setPoints(updated);
    }
}