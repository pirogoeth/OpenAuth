package me.maiome.openauth.database;

// internal
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.util.LogHandler;

// bukkit
import org.bukkit.Location;
import org.bukkit.World;

// javax Persistence
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

// ebean validation
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;

/**
 * This represents a user entry in the whitelist.
 */
@Entity
@Table(name = "whitelist")
public class DBWhitelist {
    /**
     * Name of the user, also primary id.
     */
    @Id
    @NotEmpty
    @NotNull
    private String name;

    /**
     * Location of the point.
     */
    @NotNull
    public boolean whitelisted = false;

    /**
     * Mandatory constructor for ebean use.
     */
    public DBWhitelist() { }

    /**
     * Actual constructor.
     */
    public DBWhitelist(final String name) {
        this.setName(name);
        this.save();
    }

    @Transient
    public String toString() {
        return String.format("DBWhitelist{name=%s,whitelisted=%s}", this.name, this.whitelisted);
    }

    @Transient
    public static List<DBWhitelist> getWhitelist() {
        return (List<DBWhitelist>) OpenAuth.getInstance().getDatabase().find(DBWhitelist.class).where("whitelisted == true").findList();
    }

    @Transient
    public void save() {
        if (OpenAuth.getInstance().getDatabase().find(DBWhitelist.class, this.getName()) != null) {
            return;
        }
        synchronized (OpenAuth.databaseLock) {
            OpenAuth.getInstance().getDatabase().save(this);
        }
    }

    @Transient
    public void update() {
        if (this.getName().equals(OpenAuth.getInstance().getDatabase().find(DBWhitelist.class, this.getName()).getName()) &&
            this.whitelisted == OpenAuth.getInstance().getDatabase().find(DBWhitelist.class, this.getName()).isWhitelisted()) {
            return;
        }
        synchronized (OpenAuth.databaseLock) {
            OpenAuth.getInstance().getDatabase().update(this);
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean getWhitelisted() {
        return this.whitelisted;
    }

    @Transient
    public boolean isWhitelisted() {
        return this.getWhitelisted();
    }

    public void setWhitelisted(final boolean b) {
        this.whitelisted = b;
    }

    @Transient
    public void setWhitelisted(final boolean b, final boolean update) {
        this.setWhitelisted(b);
        if (update == true) this.update();
    }

    @Transient
    public OAPlayer getPlayer() {
        return OAPlayer.getPlayer(this.name);
    }
}