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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

// internal imports
import me.maiome.openauth.actions.*;
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.bukkit.events.*;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.Permission;

public class Session {

    protected transient final int factor = (17 * 6);
    protected transient final int serial = 201;

    public long spawn_time;
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
    private Map<String, SessionData<?>> session_data = new HashMap<String, SessionData<?>>();
    private Location lloc;
    private boolean hidden = false;
    private String ip;
    private ItemStack[] inventory = new ItemStack[] { };

    protected final int wand_id = ConfigInventory.MAIN.getConfig().getInt("wand-id");

    public Session(SessionController sc, OAPlayer player) {
        this.controller = sc.getController();
        this.server = player.getServer();
        this.spawn_time = System.currentTimeMillis();
        this.sc = sc;
        this.player = player;
        if (this.freeze) {
            this.setFrozen(true);
            this.player.sendMessage(ChatColor.RED + "You must identify to continue.");
        }
        this.ip = (player.getIP() != null ? player.getIP() : "");
        OpenAuth.getOAServer().callEvent(new OASessionCreateEvent(this));
    }

    @Override
    public String toString() {
        return String.format("Session{player=%s,freeze=%b,frozen=%b,identified=%b}", this.player.getName(), this.freeze, this.frozen, this.identified);
    }

    @Override
    public int hashCode() {
        return (int) ((this.factor) + Math.abs((this.controller.hashCode() + this.sc.hashCode() + this.player.getName().hashCode() + this.wand_id + this.serial)));
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
        };

        return ((this.toString().equals(sess.toString())) && (this.hashCode() == sess.hashCode()));
    }

    public long getAge() {
        return (System.currentTimeMillis() - this.spawn_time) / 1000L;
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

    public String getIP() {
        return this.ip;
    }

    public void setIP(String ip) {
        this.ip = ip;
    }

    // identification related methods

    public boolean isIdentified() {
        return this.identified;
    }

    public boolean setIdentified(boolean identified, boolean update) {
        if (this.frozen && this.identified) {
            // this means they're trying to bypass being frozen by the stick.
            this.player.sendMessage(ChatColor.GREEN + "Sorry, but you're currently frozen, so I can't let you reidentify.");
            return false;
        }
        if (update) {
            this.frozen = (identified == true) ? false : true;
        }
        this.identified = identified;
        return true;
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

    public boolean isHidden() {
        return this.hidden;
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

    // inventory operations
    public void hideInventory() {
        Inventory playerInv = this.player.getPlayer().getInventory();
        this.inventory = playerInv.getContents();
        playerInv.clear();
        this.player.getPlayer().updateInventory();
    }

    public void unhideInventory() {
        Inventory playerInv = this.player.getPlayer().getInventory();
        playerInv.setContents(this.inventory);
        this.inventory = new ItemStack[] { };
        this.player.getPlayer().updateInventory();
    }

    // session data

    public void attachSessionData(SessionData<?> data) {
        this.session_data.put(data.getName(), data);
    }

    public SessionData<?> getSessionData(String dataname) {
        return this.session_data.get(dataname);
    }

    public void removeSessionData(String dataname) {
        this.session_data.remove(dataname);
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
        // list INDEXES are zero indexed. List.size() IS NOT. KEEP THIS IN MIND.
        for (int r = 0; r < i; r++) { // r < i because r is a zero indexed number, where i is not.
            try {
                this.actions.get(0).undo();
                this.actions.remove(0);
            } catch (java.lang.IndexOutOfBoundsException e) {
                this.player.sendMessage("All of your actions have been undone.");
                return;
            }
        }
        this.player.sendMessage(String.format("Your last %d actions have been undone, %s :)", i, this.player.getName()));
    }
}