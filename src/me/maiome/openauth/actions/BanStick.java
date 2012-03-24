package me.maiome.openauth.actions;

// bukkit imports
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.session.Session;
import me.maiome.openauth.session.SessionController;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.Permission;

public class BanStick implements Action {

    public static final String name = "ban";

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

    public void setSender(final OAPlayer sender) {
        this.sender = sender;
    }

    public void run(final OAPlayer player) {
        this.target = player;
        this.server.banPlayerByName(player);
        this.server.kickPlayer(player);
        this.used = true;
    }

    public void run(final Block block) {} //stub to complete implementation

    public void undo() {
        this.server.unbanPlayerByName(this.target);
    }
}