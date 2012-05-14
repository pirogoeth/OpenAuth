package me.maiome.openauth.session;

import com.sk89q.util.StringUtil; // string utilities.

// java imports
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
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
    private IAction action = null;
    private boolean freeze = (ConfigInventory.MAIN.getConfig().getBoolean("auth.require", false) == true) ? true : false;
    private boolean frozen;
    private boolean identified = false;
    private List<IAction> actions = new ArrayList<IAction>();
    private Location lloc;

    protected final int wand_id = ConfigInventory.MAIN.getConfig().getInt("wand-id");

    public Session (SessionController sc, OAPlayer player) {
        this.controller = sc.getController();
        this.server = player.getServer();
        this.sc = sc;
        this.player = player;
        if (this.freeze) {
            this.setFrozen(true);
            this.player.sendMessage(ChatColor.RED + "You must identify to continue.");
        }
    }

    @Override
    public String toString() {
        return String.format("Session{player=%s,freeze=%b,frozen=%b,identified=%b}", this.player.getName(), this.freeze, this.frozen, this.identified);
    }

    @Override
    public int hashCode() {
        return (int) ((17 * 6) + Math.abs((this.controller.hashCode() + this.sc.hashCode() + this.player.getName().hashCode() + this.wand_id)));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Session)) return false;
        if (obj == null) return false;

        Session sess = null;

        try {
            sess = (Session) obj;
        } catch (java.lang.ClassCastException e) {
            return false;
        }

        return (this.toString.equals(sess.toString())) && (this.hashCode() == sess.hashCode());
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

    // identification related methods

    public boolean isIdentified() {
        return this.identified;
    }

    public void setIdentified(boolean identified, boolean update) {
        if (update) {
            this.frozen = (identified == true) ? false : true;
        }
        this.identified = identified;
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public void updateFreezeState() {
        this.frozen = (this.identified == true) ? false : true;
    }

    // wand methods

    public void giveWand() {
        PlayerInventory inv = this.player.getPlayer().getInventory();
        if (!(inv.contains(new ItemStack(this.wand_id)))) {
            HashMap<Integer, ItemStack> unfitted = inv.addItem(new ItemStack(this.wand_id));
            if (unfitted.size() != 0) {
                // couldn't fit the wand into the users inventory.
                this.player.sendMessage(ChatColor.RED + "There's no more room in your inventory for the OAWand, sorry :/");
                return;
            }
        } else if (inv.contains(new ItemStack(this.wand_id))) {
            int slot = inv.first(this.wand_id);
            this.player.sendMessage(ChatColor.BLUE + String.format("You already have a wand! (check in slot %s)", slot));
            return;
        }
        this.player.getPlayer().updateInventory();
    }

    public boolean playerUsingWand() {
        return (this.player.getItemInHand() == this.wand_id && !(this.isFrozen())) ? true : false;
    }

    // login location

    public void setLoginLocation() {
        this.lloc = this.player.getLocation();
    }

    public void setLoginLocation(Location loc) {
        this.lloc = loc;
    }

    public Location getLoginLocation() {
        return this.lloc;
    }

    // action methods

    public IAction getAction() {
        return (this.action != null) ? this.action : null;
    }

    public boolean hasAction() {
        return (this.getAction() != null) ? true : false;
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
            if (this.actions.get(0).hasArgs()) {
                this.action.setArgs(this.actions.get(0).getArgs());
            }
        } catch (java.lang.Exception e) {
            this.log.warning(String.format("Exception occurred: [%s] %s", e.getClass().getCanonicalName(), e.getMessage()));
            this.action = null;
        }
    }

    public void runAction(final Entity target) {
        if (this.action != null) {
            this.action.run(target);
        } else {
            return;
        }
        this.actions.add(0, action);
        try {
            this.setAction((String) this.action.getClass().getField("name").get(this.action));
            if (this.actions.get(0).hasArgs()) {
                this.action.setArgs(this.actions.get(0).getArgs());
            }
        } catch (java.lang.Exception e) {
            this.log.warning(String.format("Exception occurred: [%s] %s", e.getClass().getCanonicalName(), e.getMessage()));
            this.action = null;
        }
    }

    public void runAction(final Block target) {
        if (this.action != null) {
            this.action.run(target);
        } else {
            return;
        }
        this.actions.add(0, action);
        try {
            this.setAction((String) this.action.getClass().getField("name").get(this.action));
            if (this.actions.get(0).hasArgs()) {
                this.action.setArgs(this.actions.get(0).getArgs());
            }
        } catch (java.lang.Exception e) {
            this.log.warning(String.format("Exception occurred: [%s] %s", e.getClass().getCanonicalName(), e.getMessage()));
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
        if (this.actions.size() == 0) {
            this.player.sendMessage(ChatColor.BLUE + "You do not have any more actions to undo.");
            return;
        }
        this.actions.get(0).undo();
        this.actions.remove(0);
    }

    public void undoLastActions(int i) {
        int n = (this.actions.size() - i);
        if (0 > n || i > this.actions.size()) {
            this.player.sendMessage(ChatColor.BLUE + "Undoing ALL of your actions, since you have given me a number that is greater than or equal to the number of actions performed.");
            for (IAction a : this.actions) {
                a.undo();
            }
            this.player.sendMessage(ChatColor.BLUE + String.format("I have undone %d actions.", this.actions.size()));
            this.actions.clear();
            return;
        }
        for (int c = 1; c <= i; c++) {
            try {
                this.actions.get((c - 1)).undo(); // converting one-indexed to zero-indexed
                this.actions.remove((c - 1)); // here too
            } catch (java.lang.IndexOutOfBoundsException e) {
                this.player.sendMessage(ChatColor.BLUE + String.format(
                    "I was only able to undo your last %d actions. Actions %d through %d don't exist.",
                    (c - 1), (c), (i)
                ));
                break;
            }
        }
    }
}