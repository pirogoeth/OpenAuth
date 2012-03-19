package me.maiome.openauth.session;

// java imports
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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

    protected final int wand_id = ConfigInventory.MAIN.getConfig().getInt("wand-id");

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

    // wand methods

    public void giveWand() {
        PlayerInventory inv = this.player.getPlayer().getInventory();
        if (!(inv.contains(new ItemStack(this.wand_id)))) {
            inv.addItem(new ItemStack(this.wand_id));
        }
        this.player.getPlayer().updateInventory();
    }

    public boolean playerUsingWand() {
        return (this.player.getItemInHand() == this.wand_id) ? true : false;
    }

    // action methods

    public Action getCurrentAction() {
        return (this.action != null) ? this.action : null;
    }

    public boolean hasAction() {
        return (this.getCurrentAction() != null) ? true : false;
    }

    public void runAction(final OAPlayer target) {
        if (this.action != null) {
            this.action.run(target);
        } else {
            return;
        }
        this.actions.add(0, action);
        try {
            this.setAction((String) this.action.getClass().getField("name").get(this.action));
        } catch (java.lang.Exception e) {
            this.action = null;
        }
    }

    public void setAction(String action) {
        this.action = Actions.getActionByName(action, this);
        if (this.action == null) {
            this.player.sendMessage(ChatColor.BLUE + String.format("Action %s does not exist.", action));
        }
    }

    public void clearAction() {
        this.action = null;
    }

    public void undoLastAction() {
        this.actions.get(0).undo(this.getPlayer());
        this.actions.remove(0);
    }
}