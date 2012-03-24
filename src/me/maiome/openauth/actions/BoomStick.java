package me.maiome.openauth.actions;

// bukkit imports
import org.bukkit.ChatColor;
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

public class BoomStick implements Action {

    public static final String name = "boom";

    private Session attached;
    private SessionController sc;
    private final String permissible = "openauth.action.boom";
    private final float power = (float) ConfigInventory.MAIN.getConfig().getDouble("actions.boom.power", 2.0D);
    private final boolean fire = ConfigInventory.MAIN.getConfig().getBoolean("actions.boom.fire", false);
    private OAServer server;
    private boolean used = false;

    protected OAPlayer sender;
    protected Object target;

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

    public boolean requiresEntityTarget() {
        return false;
    }

    public void setSender(final OAPlayer sender) {
        this.sender = sender;
    }

    public void run(final OAPlayer player) {
        this.target = player;
        player.getPlayer().getLocation().getWorld().createExplosion(
            player.getLocation(), this.power, this.fire);
        this.used = true;
    }

    public void run(final Block block) {
        this.target = block;
        block.getLocation().getWorld().createExplosion(
            block.getLocation(), this.power, this.fire);
        this.used = true;
    }

    public void undo() {
        this.sender.sendMessage(ChatColor.BLUE + "You can't undo an explosion ;p");
    }
}