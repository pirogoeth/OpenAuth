package me.maiome.openauth.actions;

// bukkit imports
import org.bukkit.Location;
import org.bukkit.World;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.session.Session;
import me.maiome.openauth.session.SessionController;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.Permission;

public class BanAction implements Action {

    private Session attached;
    private SessionController sc;
    private final String permissible = "openauth.action.ban";
    private OAServer server;

    protected OAPlayer target;

    public BanAction(OAServer server, Session attached) {
        this.server = server;
        this.sc = server.getController().getSessionController();
        this.attached = attached;
    }

    public boolean allowed() {
        return this.attached.getPlayer().hasPermission(this.permissible);
    }

    public void run(final OAPlayer player) {
        this.target = player;
        this.server.banPlayerByName(player);
    }

    public void undo(final OAPlayer player) {
        this.server.unbanPlayerByName(this.target);
    }
}