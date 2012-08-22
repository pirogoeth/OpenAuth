package me.maiome.openauth.bukkit.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.database.*;
import me.maiome.openauth.session.*;

public class OAPlayerUnbannedEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private String player;
    private DBBanRecord record;
    private boolean cancelled;

    public OAPlayerUnbannedEvent(final String player, final DBBanRecord record) {
        this.player = player;
        this.record = record;
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

    public String getPlayer() {
        return this.player;
    }

    public DBBanRecord getRecord() {
        return this.record;
    }
}