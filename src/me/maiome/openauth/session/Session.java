package me.maiome.openauth.session;

// java imports
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

// internal imports
import me.maiome.openauth.actions.*;
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.Permission;

public class Session {

    private LogHandler log = new LogHandler();
    private OpenAuth controller;
    private SessionController sc;
    private OAPlayer player;
    private OAServer server;
    private Action action = null;
    private List<Action> actions = new ArrayList<Action>();

    protected int wand_id = ConfigInventory.MAIN.getConfig().getInt("wand-id");

    public Session (SessionController sc, OAPlayer player) {
        this.controller = sc.getController();
        this.server = player.getServer();
        this.sc = sc;
        this.player = player;
    }

    public OAPlayer getPlayer() {
        return this.player;
    }

    public OAServer getServer() {
        return this.server;
    }

    public boolean isOnline() {
        return this.player.getPlayer().isOnline();
    }

    public boolean playerUsingWand() {
        return (this.player.getItemInHand() == this.wand_id) ? true : false;
    }

    public Action getCurrentAction() {
        return (this.action != null) ? this.action : null;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}