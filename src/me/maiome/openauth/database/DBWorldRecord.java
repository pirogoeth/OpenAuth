package me.maiome.openauth.database;

// internal
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.util.LogHandler;

// bukkit
import org.bukkit.Location;
import org.bukkit.World;

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
@Table(name = "worlds")
public class DBWorldRecord {
    /**
     * Name of the world, also primary id.
     */
    @Id
    @NotEmpty
    @NotNull
    private String name;

    /**
     * Gamemode for the world..
     */
    private int gamemode = 0;

    /**
     * Whether to enforce gamemode policies or not.
     */
    private boolean enforce = false;

    /**
     * Whether the world is locked down or not.
     */
    private boolean lockdown = false;

    /**
     * OAPlayer transient for use later.
     */
    @Transient
    private World world = null;

    /**
     * Mandatory constructor for ebean use.
     */
    public DBWorldRecord() { }

    /**
     * Main constructor.
     */
    private DBWorldRecord(final World world) {
        this.world = world;
        this.setName(world.getName());
        this.save();
    }

    @Transient
    private void save() {
        synchronized (OpenAuth.databaseLock) {
            OpenAuth.getInstance().getDatabase().save(this);
        }
    }

    @Transient
    public void update() {
        synchronized (OpenAuth.databaseLock) {
            try {
                OpenAuth.getInstance().getDatabase().update(this);
            } catch (java.lang.Exception e) {
                LogHandler.exDebug("Error updating DBWorldRecord [" + this.name + "]: " + e.getMessage());
                return;
            }
            LogHandler.exDebug("Successfully updated DBWorldRecord [" + this.name + "].");
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getGamemode() {
        return this.gamemode;
    }

    public void setGamemode(final int gm) {
        this.gamemode = gm;
    }

    public boolean getEnforce() {
        return this.enforce;
    }

    @Transient
    public boolean isEnforce() {
        return this.getEnforce();
    }

    public void setEnforce(boolean b) {
        this.enforce = b;
    }

    public boolean getLockdown() {
        return this.lockdown;
    }

    public void setLockdown(boolean b) {
        this.lockdown = b;
    }

    @Transient
    private void setWorld(final World w) throws UnsupportedOperationException {
        if (this.world != null) {
            throw new UnsupportedOperationException("Cannot reset the world assiciated to a DBWorldRecord.");
        }
        this.world = w;
    }

    @Transient
    public static DBWorldRecord getWorldRecord(final World w) {
        return getWorldRecord(w.getName());
    }

    @Transient
    public static DBWorldRecord getWorldRecord(final String s) {
        DBWorldRecord record = OpenAuth.getInstance().getDatabase().find(DBWorldRecord.class, s);
        if (record == null) {
            record = new DBWorldRecord(OpenAuth.getInstance().getServer().getWorld(s));
        }
        return record;
    }
}