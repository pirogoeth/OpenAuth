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
     * OAPlayer transient for use later.
     */
    @Transient
    private OAPlayer player;

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
                LogHandler.exDebug("Error updating DBPlayer [" + this.name + "]: " + e.getMessage());
                return;
            }
            LogHandler.exDebug("Successfully updated DBPlayer [" + this.name + "].");
        }
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

    public void setPassword(final String password) {
        this.password = password;
    }

    @Transient
    public void setPassword(final String password, final boolean update) {
        this.setPassword(password);
        if (update == true) this.update();
    }

    public String getPassword() {
        return this.password;
    }
}