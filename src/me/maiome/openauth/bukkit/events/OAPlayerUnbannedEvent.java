package me.maiome.openauth.bukkit.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.session.*;

public class OAPlayerUnbannedEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Object player;
    private String reason;
    private boolean cancelled;

    public OAPlayerUnbannedEvent(final OAPlayer player, final String reason) {
        this.player = player;
        this.reason = reason;
    }

    public OAPlayerUnbannedEvent(final String player, final String reason) {
        this.player = player;
        this.reason = reason;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean c) {
        this.cancelled = c;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Object getPlayer() {
        if (this.player.getClass().getCanonicalName().equals("me.maiome.openauth.bukkit.OAPlayer")) {
            return (OAPlayer) this.player;
        } else if (this.player.getClass().getCanonicalName().equals("java.lang.String")) {
            return (String) this.player;
        } else {
            return null;
        }
    }

    public String getReason() {
        return this.reason;
    }
}