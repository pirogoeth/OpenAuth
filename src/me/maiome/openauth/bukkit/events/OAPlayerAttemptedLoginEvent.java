package me.maiome.openauth.bukkit.event;

import org.bukkit.event.Event;

public class OAPlayerAttemptedLoginEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private OAPlayer player;
    private boolean success;

    public OAPlayerAttemptedLoginEvent(OAPlayer player, boolean success) {
        this.player = player;
        this.success = success;
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

    public boolean loginSucceeded() {
        return this.success;
    }
}