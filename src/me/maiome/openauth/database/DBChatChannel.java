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

}