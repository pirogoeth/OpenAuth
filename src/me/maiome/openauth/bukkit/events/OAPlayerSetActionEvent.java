package me.maiome.openauth.bukkit.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.maiome.openauth.actions.IAction;
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.session.*;

public class OAPlayerSetActionEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private IAction action;
    private OAPlayer player;
    private Session session;
    protected boolean cancelled;

    public OAPlayerSetActionEvent(OAPlayer player, Session attached, IAction action) {
        this.player = player;
        this.session = attached;
        this.action = action;
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

    public IAction getAction() {
        return this.action;
    }

    public OAPlayer getPlayer() {
        return this.player;
    }

    public Session getSession() {
        return this.session;
    }
}