package me.maiome.openauth.actions;

import com.sk89q.util.StringUtil; // string util

// bukkit imports
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.metrics.Tracker;
import me.maiome.openauth.session.Session;
import me.maiome.openauth.session.SessionController;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.Permission;

public class BanStick implements IAction {

    protected final int factor = (17 * 7);
    protected final int serial = 300;

    public static final String name = "ban";
    public static final Tracker tracker = new Tracker("BanStick");

    private String[] args = null;
    private Session attached;
    private SessionController sc;
    private final String permissible = "openauth.action.ban";
    private OAServer server;
    private boolean used = false;

    protected OAPlayer target;
    protected OAPlayer sender;

    public BanStick(OAServer server, Session attached) {
        this.server = server;
        this.sc = server.getController().getSessionController();
        this.attached = attached;
        this.setSender(this.attached.getPlayer());
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return String.format("BanStick{permissible=%s}", this.permissible);
    }

    public int hashCode() {
        return (int) Math.abs(((this.factor) + (this.server.hashCode() + this.attached.hashCode() + this.serial)));
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
        return true;
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

    public void setSender(final OAPlayer sender) {
        this.sender = sender;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public void run(final OAPlayer player) {
        this.target = player;
        this.server.banPlayerByName(player);
        tracker.increment();
        if (this.args != null) {
            this.server.kickPlayer(player, StringUtil.joinString(this.args, " "));
        } else {
            this.server.kickPlayer(player);
        }
        this.used = true;
    }

    public void run(final Block block) {} //stub to complete implementation
    public void run(final Entity entity) {} //stub to complete implementation

    public void undo() {
        this.server.unbanPlayerByName(this.target);
    }
}