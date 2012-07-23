package me.maiome.openauth.bukkit.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.session.*;

public class OAPlayerLoginEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private OAPlayer player;
    private boolean cancelled = false;
    private String cancelreason = "No reason given.";

    public OAPlayerLoginEvent(OAPlayer player) {
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

    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelReason(String s) {
        this.cancelreason = s;
    }

    public String getCancelReason() {
        return this.cancelreason;
    }
}