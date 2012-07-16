package me.maiome.openauth.bukkit.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.session.*;

public class OASessionDestroyEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Session session;
    private boolean cancelled = false;

    public OASessionDestroyEvent(Session session) {
        this.session = session;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public void setCancelled(boolean c) {
        this.cancelled = c;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Session getSession() {
        return this.session;
    }

    public SessionData getSessionData(String dataname) {
        return this.session.getSessionData(dataname);
    }
}