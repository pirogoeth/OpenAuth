package me.maiome.openauth.bukkit.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.bukkit.OAPlayer.PlayerState;
import me.maiome.openauth.session.*;

public class OAPlayerWhitelistedEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private String player;
    private boolean cancelled;

    public OAPlayerWhitelistedEvent(String name) {
        this.player = player;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public String getPlayer() {
        return this.player;
    }

    public OAPlayer attemptPlayerWrap() {
        try {
            OAPlayer p = OpenAuth.getOAServer().getController().wrap(this.player);
            return ((p != null) ? p : null);
        } catch (java.lang.Exception e) {
            return null;
        }
    }
}