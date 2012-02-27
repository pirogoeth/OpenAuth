package me.maiome.openauth.bukkit.events;

// bukkit
import org.bukkit.event.Event;
import org.bukkit.event.HandlerLiÂst;

// internal
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.util.LogHandler;

public class PlayerStateChangeEvent extends Event {

    private final OAPlayer target;
    private static final HandlerList handlers = new HandlerList();

    public PlayerStateChangeEvent(OAPlayer player) {
        this.target = player;
    }

    public OAPlayer getPlayer() {
        return this.target;
    }

    public HandlerList getHandlers() {
        return this.handlers;
    }

    public static HandlerList getHandlerList() {
        return this.handlers;
    }
}