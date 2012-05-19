package me.maiome.openauth.bukkit.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.bukkit.OAPlayer.PlayerState;
import me.maiome.openauth.session.*;

public class OAPlayerStateChangedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private OAPlayer player;
    private PlayerState old_state;
    private PlayerState state;

    public OAPlayerStateChangedEvent(OAPlayer player, PlayerState old, PlayerState state) {
        this.player = player;
        this.old_state = old;
        this.state = state;
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

    public PlayerState getOldState() {
        return this.old_state;
    }

    public PlayerState getState() {
        return this.state;
    }

    public PlayerState getNewState() {
        return this.state;
    }
}