package me.maiome.openauth.database;

// internal
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.LogHandler;

// bukkit
import org.bukkit.Location;
import org.bukkit.Server;

// java
import java.util.Arrays;
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

@Entity
@Table(name = "channels")
public class DBChatChannel {

    /**
     * Name of the channel.
     */
    @Id
    @NotEmpty
    @NotNull
    public String name;

    /**
     * Name of the channel owner.
     */
    @NotEmpty
    @NotNull
    public String owner;

    /**
     * Members of the channel.
     * List string is formatted like this:
     *   member1,member2,member3,etc
     */
    @NotEmpty
    @NotNull
    public String members;

    /**
     * If the channel is hidden in the list from everyone without the correct permissions.
     */
    @NotNull
    public boolean hidden = false;

    /**
     * Whether the channel is invite only or not.
     */
    @NotNull
    public boolean invite = false;

    /**
     * Topicline of the channel.
     */
    @NotEmpty
    @NotNull
    public String topic;

    /**
     * Channel join password.
     */
    @NotNull
    public String password = "";

    @Transient
    public OAPlayer player = null;

    public DBChatChannel() { }; // stub for ebean

    public DBChatChannel(OAPlayer owner, String name) {
        this.player = owner;
        this.setName(name);
        this.save();
    }

    public synchronized void save() {
        synchronized (OpenAuth.databaseLock) {
            OpenAuth.getInstance().getDatabase().save(this);
        }
    }

    public synchronized void update() {
        synchronized (OpenAuth.databaseLock) {
           try {
               OpenAuth.getInstance().getDatabase().update(this);
           } catch (java.lang.Exception e) {
               LogHandler.exDebug("Error updating DBChatChannel [" + this.name + "]: " + e.getMessage());
               return;
           }
           LogHandler.exDebug("Successfully updated DBChatChannel [" + this.name + "].");
       }
   }

    public String getName() {
        return this.name;
    }

    public String getOwner() {
        return this.owner;
    }

    public String getMembers() {
        return this.members;
    }

    public boolean getHidden() {
        return this.hidden;
    }

    public boolean getInvite() {
        return this.invite;
    }

    public String getTopic() {
        return this.topic;
    }

    public String getPassword() {
        return this.password;
    }

    @Transient
    public String[] getMembersAsArray() {
        return this.members.split("\\,");
    }

    @Transient
    public boolean hasPlayer(String player) {
        List<String> members = Arrays.asList(this.getMembersAsArray());
        return members.contains(player);
    }

    @Transient
    public boolean hasPlayer(OAPlayer player) {
        return this.hasPlayer(player.getName());
    }

    @Transient
    public OAPlayer getOwnerPlayerInstance() {
        return this.player;
    }

    public void setName(String s) {
        this.name = s;
    }

    public void setOwner(String s) {
        this.owner = s;
    }

    public void setMembers(String s) {
        this.members = s;
    }

    public void setHidden(boolean b) {
        this.hidden = b;
    }

    public void setInvite(boolean b) {
        this.invite = b;
    }

    public void setTopic(String s) {
        this.topic = s;
    }

    public void setPassword(String s) {
        this.password = s;
    }

    @Transient
    public void setMembersFromArray(String[] s) {
        StringBuilder members = new StringBuilder();
        if (s.length > 1) {
            for (int i = 0; i < (s.length - 1); i++) {
                members.append(s[i] + ",");
            }
            members.append(s[s.length - 1]);
        } else {
            members.append(s[0]);
        }
        this.setMembers(members.toString());
    }
}