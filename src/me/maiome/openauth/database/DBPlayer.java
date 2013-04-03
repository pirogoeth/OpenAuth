package me.maiome.openauth.database;

// internal
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.util.LogHandler;

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
import com.avaje.ebean.*;
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
     * User's logon password, hashed.
     */
    private String password = null;

    /**
     * User's HKAuth id.
     */
    private String hkey = null;

    /**
     * OAPlayer transient for use later.
     */
    @Transient
    private OAPlayer player;

    @Transient
    public static synchronized void clean() {
        try {
            SqlQuery affecting = OpenAuth.getInstance().getDatabase().createSqlQuery("select * from users where password is null or password = '';");
            List<SqlRow> affected = affecting.findList();
            // new weird method that's probably very slow.
            for (SqlRow row : affected) {
                DBPlayer pl = OpenAuth.getInstance().getDatabase().find(DBPlayer.class, row.getString("name"));
                pl.delete();
            }
            // this is the old method that SHOULD'VE WORKED, but doesn't. should've worked because it works directly in sqlite3/sql.
            // but doesn't work here for some reason.
            //
            // SqlQuery query = OpenAuth.getInstance().getDatabase().createSqlQuery("delete from users where password is null or password = '';");
            LogHandler.debug("[DB] Purged user table of " + affected.size() + " rows.");
        } catch (java.lang.Exception e) {
            LogHandler.debug("Error occurred while purging user table.");
            e.printStackTrace();
        }
    }

    /**
     * Mandatory constructor for ebean use.
     */
    public DBPlayer() { }

    /**
     * Main constructor.
     */
    public DBPlayer(final OAPlayer player) {
        this.player = player;
        this.setName(player.getName());
        this.save();
    }

    /**
     * Alternate constructor.
     */
    public DBPlayer(final String player) {
        this.setName(player);
        this.save();
    }

    @Transient
    public void save() {
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
                LogHandler.debug("Error updating DBPlayer [" + this.name + "]: " + e.getMessage());
                return;
            }
            LogHandler.debug("Successfully updated DBPlayer [" + this.name + "].");
        }
    }

    @Transient
    public void delete() {
        synchronized (OpenAuth.databaseLock) {
            try {
                OpenAuth.getInstance().getDatabase().delete(this);
            } catch (java.lang.Exception e) {
                LogHandler.debug("Error deleting DBPlayer [" + this.name + "]: " + e.getMessage());
                return;
            }
            LogHandler.debug("Successfully deleted DBPlayer [" + this.name + "].");
        }
    }

    @Transient
    public OAPlayer getPlayer() {
        return OAPlayer.getPlayer(this.getName());
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Transient
    public void setName(final String name, final boolean update) {
        this.setName(name);
        if (update == true) this.update();
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    @Transient
    public void setPassword(final String password, final boolean update) {
        this.setPassword(password);
        if (update == true) this.update();
    }

    public String getHkey() {
        return this.hkey;
    }

    public void setHkey(String hkey) {
        this.hkey = hkey;
    }

    @Transient
    public void setHkey(String hkey, final boolean update) {
        this.setHkey(hkey);
        if (update == true) this.update();
    }
}