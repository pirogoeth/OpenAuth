package me.maiome.openauth.bukkit.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.session.*;

public class OAPlayerRegistrationEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Object player;

    public OAPlayerRegistrationEvent(Object player) {
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Object getPlayer() {
        if (this.player instanceof OAPlayer) {
            return (OAPlayer) this.player;
        } else if (this.player instanceof String) {
            return (String) this.player;
        } else {
            return null;
        }
    }
}