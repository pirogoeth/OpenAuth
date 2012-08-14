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
    @Column(name = "OID")
    @NotEmpty
    public int id = null;

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
    public boolean loginsuccess;

    /**
     * Holds the last login time.
     */
    @NotNull
    public long lastlogin;

    /**
     * Holds the last login IP.
     */
    @NotNull
    public String lastloginip;

    /**
     * Holds the number of times this session has been reused before disposal.
     */
    @NotNull
    public int reusecount = 0;

    /**
     * Holds the time that the session was created.
     */
    @NotNull
    public long starttime = 0L;

    /**
     * Holds the time that this session was destroyed.
     */
    @NotNull
    public long closetime = 0L;

    /**
     * Holds the number of blocks that were placed during this session.
     */
    @NotNull
    public long blocksplaced = 0;

    /**
     * Holds the number of blocks that were broken during this session.
     */
    @NotNull
    public long blocksdestroyed = 0;

    /**
     * Holds the last known location of a player using this session.
     */
    @NotNull
    public String lastlocation = "";

    @Transient
    public Session session;

    public DBSessionRecord() { }; // for ebean

    public DBSessionRecord(Session session) {
        this.session = session;
        this.setId(null);
        this.setName(this.session.getPlayer().getName());
        this.setStarttime(this.session.spawn_time);
        this.setLastlogin(this.session.spawn_time);
        this.setLastloginip(this.session.getIP());
    }

    public synchronized void save() {
        OpenAuth.getInstance().getDatabase().save(this);
    }

    public synchronized void update() {
        try {
            OpenAuth.getInstance().getDatabase().update(this);
        } catch (java.lang.Exception e) {
            LogHandler.exDebug(String.format("Error updating DBSessionRecord{name=%s,id=%s,start=%s,success=%b}", this.getName(), this.getId(), this.getStarttime(), this.getLoginsuccess()));
            return;
        }
        LogHandler.exDebug(String.format("Successfully updated DBSessionRecord{name=%s,id=%s,start=%s,success=%b}", this.getName(), this.getId(), this.getStarttime(), this.getLoginsuccess()));
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

    public boolean getLoginsuccess() {
        return this.loginsuccess;
    }

    public long getLastlogin() {
        return this.lastlogin;
    }

    public String getLastloginip() {
        return this.lastloginip;
    }

    public int getReusecount() {
        return this.reusecount;
    }

    public long getStarttime() {
        return this.starttime;
    }

    public long getClosetime() {
        return this.closetime;
    }

    @Transient
    public long getAge() {
        if (this.getClosetime() == 0L) {
            return (System.currentTimeMillis() - this.getStarttime());
        } else {
            return (this.getClosetime() - this.getStarttime());
        }
    }

    public long getBlocksplaced() {
        return this.blocksplaced;
    }

    public long getBlocksdestroyed() {
        return this.blocksdestroyed;
    }

    public String getLastlocation() {
        return this.lastlocation;
    }

    public void setId(final int i) {
        this.id = i;
    }

    public void setName(final String s) {
        this.name = s;
    }

    public void setLoginsuccess(final boolean b) {
        this.loginsuccess = b;
    }

    public void setLastlogin(final long l) {
        this.lastlogin = l;
    }

    public void setLastloginip(final String s) {
        this.lastloginip = s;
    }

    public void setReusecount(final int l) {
        this.reusecount = l;
    }

    public void setStarttime(final long l) {
        this.starttime = l;
    }

    public void setClosetime(final long l) {
        this.closetime = l;
    }

    public void setBlocksplaced(final long l) {
        this.blocksplaced = l;
    }

    public void setBlocksdestroyed(final long l) {
        this.blocksdestroyed = l;
    }

    public void setLastlocation(final String s) {
        this.lastlocation = s;
    }

    @Transient
    public void setLastLocation(final Location loc) {
        String locstring = String.format("%s@%d,%d,%d:%d,%d", loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        this.setLastlocation(locstring);
    }

    @Transient
    public Location getLastLocationAsLoc() {
        final String locstr = this.getLastlocation();
        String world = locstr.split("\\@")[0];
        String[] locs = locstr.split("\\@")[1].split("\\:")[0].split("\\,");
        double x = new Double(locs[0]), y = new Double(locs[1]), z = new Double(locs[2]);
        String[] los = locstr.split("\\:")[1].split("\\,");
        float yaw = new Float(los[0]), pitch = new Float(los[1]);
        return new Location(Bukkit.getServer().getWorld(world), x, y, z, yaw, pitch);
    }
}