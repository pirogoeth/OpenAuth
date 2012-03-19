package me.maiome.openauth.actions;

// bukkit imports
import org.bukkit.ChatColor;
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

public class BoomStick implements Action {

    public static final String name = "boom";

    private Session attached;
    private SessionController sc;
    private final String permissible = "openauth.action.boom";
    private final float power = 5.3F;
    private OAServer server;
    private boolean used = false;

    protected OAPlayer target;

    public BoomStick(OAServer server, Session attached) {
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

    public void run(final OAPlayer player) {
        this.target = player;
        this.target.getPlayer().getLocation().getWorld().createExplosion(
            this.target.getLocation(), this.power, true);
        this.used = true;
    }

    public void undo(final OAPlayer player) {
        player.getPlayer().sendMessage(ChatColor.BLUE + "You can't undo an explosion ;p");
    }
}