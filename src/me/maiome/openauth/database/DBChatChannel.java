package me.maiome.openauth.database;

// internal
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.LogHandler;

// bukkit
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

@Entity
@Table(name = "channels")
public class DBChatChannel {

    @Id
    @NotEmpty
    @NotNull
    public String name;

    @NotEmpty
    @NotNull
    public String owner;

    @NotEmpty
    @NotNull
    public String members;

    @NotEmpty
    @NotNull
    public char[] flags;

    @NotEmpty
    @NotNull
    public String topic;

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

    public char[] getFlags() {
        return this.flags;
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

    public void setFlags(char[] c) {
        this.flags = c;
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