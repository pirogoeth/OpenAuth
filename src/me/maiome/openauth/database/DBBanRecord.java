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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// javax Persistence
import javax.persistence.*;

// ebean validation
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;

@Entity
@Table(name = "bans")
public class DBBanRecord {

    /**
     * Name of the player that's being banned.
     */
    @Id
    @NotEmpty
    @NotNull
    private String name;

    /**
     * Type of ban, 1 for name, 2 for IP.
     */
    @NotNull
    private int type;

    /**
     * Reason for ban.
     */
    @NotEmpty
    @NotNull
    private String reason = "No reason given.";

    /**
     * Who placed the ban.
     */
    @NotEmpty
    @NotNull
    private String banner;

    /**
     * Time the ban was placed, (seconds since the zero epoch)
     */
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date time = new Date();

    /**
     * String of data the player is bannable by.
     * If type is 1, this will be their name.
     * If type is 2, this will be the player's IP.
     */
    @NotEmpty
    @NotNull
    private String bannable;

    public DBBanRecord() { };

    public DBBanRecord(OAPlayer banee, int type, String banner, String reason) {
        this(banee.getName(), type, banner, reason, banee.getIP());
    }

    public DBBanRecord(String banee, int type, String banner, String reason, String IP) throws IllegalArgumentException {
        this.setName(banee);
        if (type != 1 && type != 2) {
            throw new IllegalArgumentException("Ban type must be either 1 or 2.");
        }
        this.setType(type);
        this.setBanner(banner);
        this.setReason(reason);
        switch (this.type) {
            case 1:
                this.setBannable(this.getName());
                break;
            case 2:
                this.setBannable(IP);
                break;
        }
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
                LogHandler.debug("Error while updating DBBanRecord [" + this.getName() + "]: " + e.getMessage());
                return;
            }
            LogHandler.debug("Successfully updated DBBanRecord [" + this.getName() + "]!");
        }
    }

    @Transient
    public void delete() {
        synchronized (OpenAuth.databaseLock) {
            try {
                OpenAuth.getInstance().getDatabase().delete(this);
            } catch (java.lang.Exception e) {
                LogHandler.debug("Error while deleting DBBanRecord [" + this.getName() + "]: " + e.getMessage());
                return;
            }
            LogHandler.debug("Successfully deleted DBBanRecord [" + this.getName() + "]!");
        }
    }

    public String getName() {
        return this.name;
    }

    public int getType() {
        return this.type;
    }

    public String getReason() {
        return this.reason;
    }

    public String getBanner() {
        return this.banner;
    }

    public Date getTime() {
        return this.time;
    }

    public String getBannable() {
        return this.bannable;
    }

    public void setName(String s) {
        this.name = s;
    }

    public void setType(int i) {
        this.type = i;
    }

    public void setReason(String s) {
        this.reason = s;
    }

    public void setBanner(String s) {
        this.banner = s;
    }

    public void setTime(Date d) {
        this.time = d;
    }

    public void setBannable(String s) {
        this.bannable = s;
    }
}