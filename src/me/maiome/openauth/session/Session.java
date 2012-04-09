package me.maiome.openauth.session;

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
    private Action action = null;
    private boolean freeze = (ConfigInventory.MAIN.getConfig().getBoolean("auth.require", false) == true) ? true : false;
    private boolean frozen;
    private boolean identified = false;
    private List<Action> actions = new ArrayList<Action>();
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
        this.frozen = (identified == true) ? false : true;
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
            // int slot = inv.first(this.wand_id);
            inv.setItemInHand(new ItemStack(this.wand_id));
            this.player.sendMessage(ChatColor.BLUE + "You already have a wand!");
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

    public Action getAction() {
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
        } catch (java.lang.Exception e) {
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
        } catch (java.lang.Exception e) {
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
        this.actions.get(0).undo();
        this.actions.remove(0);
    }

    public void undoLastActions(int i) {
        for (int c = 0; c <= (i - 1); c++) { // using (i - 1) since lists are zero indexed.
            try {
                this.actions.get(c).undo();
                this.actions.remove(c);
            } catch (java.lang.IndexOutOfBoundsException e) {
                this.player.sendMessage(ChatColor.BLUE + String.format(
                    "I was only able to undo your last %d actions. Actions %d through %d don't exist.",
                    (c), (c + 1), (i)
                ));
                break;
            }
        }
    }
}