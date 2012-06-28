package me.maiome.openauth.bukkit.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.session.*;

public class OASessionCreateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Session session;

    public OASessionCreateEvent(Session session) {
        this.session = session;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Session getSession() {
        return this.session;
    }

    public void attachSessionData(SessionData data) {
        this.session.attachSessionData(data);
    }

    public SessionData getSessionData(String dataname) {
        return this.session.getSessionData(dataname);
    }

    public void removeSessionData(String name) {
        this.session.removeSessionData(name);
    }
}