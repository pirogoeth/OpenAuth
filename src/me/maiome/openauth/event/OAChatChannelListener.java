package me.maiome.openauth.event;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.bukkit.event.*;
import me.maiome.openauth.chat.ChannelManager;
import me.maiome.openauth.util.*;

import org.bukkit.event.*;
import org.bukkit.event.Event.Result;
import org.bukkit.event.player.*;

public class OAChatChannelListener implements Listener {

    private final LogHandler log = new LogHandler();
    private final OpenAuth controller = (OpenAuth) OpenAuth.getInstance();
    private ChannelManager manager;

    public OAChatChannelListener() {
        this.manager = ChannelManager.getManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(PlayerChatEvent event) {
        if (!(event.isCancelled())) {
            this.manager.process(event.getPlayer(), event.getMessage());
            event.setCancelled();
        }
    }
}