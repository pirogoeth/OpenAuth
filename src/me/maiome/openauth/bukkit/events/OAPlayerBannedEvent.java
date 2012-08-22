package me.maiome.openauth.bukkit.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.database.*;
import me.maiome.openauth.session.*;

public class OAPlayerBannedEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Object player;
    private DBBanRecord record;
    private boolean cancelled;

    public OAPlayerBannedEvent(final OAPlayer player, final DBBanRecord record) {
        this.player = player;
        this.record = record;
    }

    public OAPlayerBannedEvent(final String player, final DBBanRecord record) {
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

    public Object getPlayer() {
        if (this.player.getClass().getCanonicalName().equals("me.maiome.openauth.bukkit.OAPlayer")) {
            return (OAPlayer) this.player;
        } else if (this.player.getClass().getCanonicalName().equals("java.lang.String")) {
            return (String) this.player;
        } else {
            return null;
        }
    }

    public DBBanRecord getRecord() {
        return this.record;
    }
}