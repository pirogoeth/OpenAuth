package me.maiome.openauth.chat;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.database.*;
import me.maiome.openauth.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelManager {

    public static ChannelManager instance;
    public Map<String, DBChatChannel> channels = new HashMap<String, DBChatChannel>(); // map: name => channel info
    public Map<OAPlayer, String> players = new HashMap<OAPlayer, String>(); // map: player => channel name
    public List<DBChatChannel> chanlist = new ArrayList<DBChatChannel>(); // list of channels

    /**
     * Returns the current instance of the chatmanager.
     */
    public static ChannelManager getManager() {
        return instance;
    }

    public ChannelManager() {
        if (ConfigInventory.MAIN.getConfig().getBoolean("chatchannels.enabled", false) == true) {
            this.initialise();
        }
    }

    private void initialise() {
        List<DBChatChannel> chans = OpenAuth.getInstace().getDatabase().find(DBChatChannel.class).findList();
    }
}