package me.maiome.openauth.actions;

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.session.Session;
import me.maiome.openauth.session.SessionController;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.Permission;

public class FreezeStick implements IAction {

    public static final String name = "freeze";

    protected final int factor = (17 * 7);
    protected final int serial = 302;

    private String[] args = null;
    private Session attached;
    private SessionController sc;
    private final LogHandler log = new LogHandler();
    private final String permissible = "openauth.action.freeze";
    private OAServer server;
    private boolean used = false;

    protected OAPlayer sender;
    protected OAPlayer target;

    public FreezeStick(OAServer server, Session attached) {
        this.server = server;
        this.sc = server.getController().getSessionController();
        this.attached = attached;
        this.setSender(this.attached.getPlayer());
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return String.format("FreezeStick{permissible=%s}", this.permissible);
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