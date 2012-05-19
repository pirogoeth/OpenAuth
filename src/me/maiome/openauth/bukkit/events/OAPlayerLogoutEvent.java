package me.maiome.openauth.bukkit.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.session.*;

public class OAPlayerLogoutEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private OAPlayer player;

    public OAPlayerLogoutEvent(OAPlayer player) {
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public OAPlayer getPlayer() {
        return this.player;
    }
}