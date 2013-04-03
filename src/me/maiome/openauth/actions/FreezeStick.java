package me.maiome.openauth.actions;

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.metrics.Tracker;
import me.maiome.openauth.session.Session;
import me.maiome.openauth.session.SessionController;
import me.maiome.openauth.util.*;

public class FreezeStick implements IAction {

    public static final String name = "freeze";
    public static final Tracker tracker = new Tracker("FreezeStick");

    private final SessionController sc = SessionController.getInstance();
    private final LogHandler log = new LogHandler();
    private final String permissible = "openauth.action.freeze";
    private final OAServer server = OAServer.getInstance();
    private String[] args = null;
    private Session attached;
    private boolean used = false;

    protected OAPlayer sender;
    protected OAPlayer target;

    public FreezeStick() { }

    public FreezeStick(Session attached) {
        this.attached = attached;
        this.setSender(this.attached.getPlayer());
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return String.format("FreezeStick{permissible=%s}", this.permissible);
    }

    public boolean allowed() {
        return this.attached.getPlayer().hasPermission(this.permissible);
    }

    public boolean isUsed() {
        return this.used;
    }

    public boolean requiresEntityTarget() {
        return true;
    }

    public boolean allowsAnyEntityTarget() {
        return false;
    }

    public boolean allowsArgs() {
        return false;
    }

    public String[] getArgs() {
        return this.args;
    }

    public boolean hasArgs() {
        return (this.args != null);
    }

    public boolean requiresArgs() {
        return false;
    }

    public void setSender(final OAPlayer player) {
        this.sender = player;
    }

    public void setArgs(String[] args) {}

    public void run(final OAPlayer player) {
        this.target = player;
        tracker.increment();
        this.target.getSession().setFrozen(true);
        this.target.sendMessage(String.format("%sYou have been frozen by %s.", ChatColor.BLUE, this.sender.getName()));
        this.used = true;
    }

    public void run(final Block block) {}
    public void run(final Entity entity) {}

    public void undo() {
        this.target.getSession().setFrozen(false);
        this.target.sendMessage(String.format("%sYou have been unfrozen.", ChatColor.BLUE));
    }
}