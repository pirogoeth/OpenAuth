package me.maiome.openauth.database;

// internal
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.LogHandler;

// bukkit
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;

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
 * This holds session records.
 */
@Entity
@Table(name = "records")
public class DBSessionRecord {

    /**
     * Holds the generated id.
     */
    @Id
    @NotNull
    @NotEmpty
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public long id;

    /**
     * This will hold the session name.
     */
    @NotNull
    @NotEmpty
    public String name;

    /**
     * Holds login success.
     */
    @NotNull
    public boolean login_success;

    /**
     * Holds the last login time.
     */
    @NotNull
    @NotEmpty
    public long last_login;

    /**
     * Holds the number of times this session has been reused before disposal.
     */
    @NotNull
    public int reuse_count = 0;

    /**
     * Holds the time that the session was created.
     */
    @NotEmpty
    @NotNull
    public long start_time;

    /**
     * Holds the time that this session was destroyed.
     */
    @NotEmpty
    @NotNull
    public long close_time;

    /**
     * Holds the age of the session (in seconds).
     */
    @NotEmpty
    @NotNull
    public long age;

    /**
     * Holds the number of blocks that were placed during this session.
     */
    @NotEmpty
    @NotNull
    public long blocks_placed = 0;

    /**
     * Holds the number of blocks that were broken during this session.
     */
    @NotEmpty
    @NotNull
    public long blocks_destroyed = 0;

    /**
     * Holds the last known location of a player using this session.
     */
    @NotEmpty
    @NotNull
    public String last_location = "";

    @Transient
    public Session session;

    public DBSessionRecord() { }; // for ebean

    public DBSessionRecord(Session session) {
        this.session = session;
    }

    @Transient
    public Session getSession() {
        return this.session;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public boolean getLoginSuccess() {
        return this.login_success;
    }

    public long getLastLogin() {
        return this.last_login;
    }

    public int getReuseCount() {
        return this.reuse_count;
    }

    public long getStartTime() {
        return this.start_time;
    }

    public long getCloseTime() {
        return this.close_time;
    }

    public long getAge() {
        return this.age;
    }

    public long getBlocksPlaced() {
        return this.blocks_placed;
    }

    public long getBlocksDestroyed() {
        return this.blocks_destroyed;
    }

    public String getLastLocation() {
        return this.last_location;
    }

    public void setId(long l) {
        this.id = l;
    }

    public void setName(String s) {
        this.name = s;
    }

    public void setLoginSuccess(boolean b) {
        this.login_success = b;
    }

    public void setLastLogin(long l) {
        this.last_login = l;
    }

    public void setReuseCount(int l) {
        this.reuse_count = l;
    }

    public void setStartTime(long l) {
        this.start_time = l;
    }

    public void setCloseTime(long l) {
        this.close_time = l;
    }

    public void setAge(long l) {
        this.age = l;
    }

    public void setBlocksPlaced(long l) {
        this.blocks_placed = l;
    }

    public void setBlocksDestroyed(long l) {
        this.blocks_destroyed = l;
    }

    public void setLastLocation(String s) {
        this.last_location = s;
    }

    @Transient
    public void setLastLocation(final Location loc) {
        String locstring = String.format("%s@%d,%d,%d:%d,%d", loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        this.setLastLocation(locstring);
    }

    @Transient
    public Location getLastLocationAsLoc() {
        final String locstr = this.getLastLocation();
        String world = locstr.split("\\@")[0];
        String[] locs = locstr.split("\\@")[1].split("\\:")[0].split("\\,");
        double x = new Double(locs[0]), y = new Double(locs[1]), z = new Double(locs[2]);
        String[] los = locstr.split("\\:")[1].split("\\,");
        float yaw = new Float(los[0]), pitch = new Float(los[1]);
        return new Location(Bukkit.getServer().getWorld(world), x, y, z, yaw, pitch);
    }
}