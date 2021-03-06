package me.maiome.openauth.actions;

import com.sk89q.util.StringUtil; // string util

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;

// java imports
import java.util.Iterator;
import java.util.List;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.event.OAExplosionListener;
import me.maiome.openauth.metrics.Tracker;
import me.maiome.openauth.session.Session;
import me.maiome.openauth.session.SessionController;
import me.maiome.openauth.util.*;

public class BoomStick implements IAction {

    public static final String name = "boom";
    public static final Tracker tracker = new Tracker("BoomStick");

    private final boolean fire = Config.getConfig().getBoolean("actions.boom.fire", false);
    private final boolean acruelty = Config.getConfig().getBoolean("actions.boom.animal-cruelty", false);
    private final boolean gcruelty = Config.getConfig().getBoolean("actions.boom.golem-cruelty", false);
    private final OAServer server = OAServer.getInstance();
    private final SessionController sc = SessionController.getInstance();
    private final LogHandler log = new LogHandler();
    private final String permissible = "openauth.action.boom";
    private String[] args = null;
    private Session attached;
    private float power = (float) Config.getConfig().getDouble("actions.boom.power", 2.0D);
    private boolean used = false;

    protected OAPlayer sender;
    protected Object target;
    protected Location t_location;

    public BoomStick() { }

    public BoomStick(Session attached) {
        this.attached = attached;
        this.setSender(this.attached.getPlayer());
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return String.format("BoomStick{permissible=%s}", this.permissible);
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

    public boolean allowsAnyEntityTarget() {
        return true;
    }

    public boolean allowsArgs() {
        return true;
    }

    public String[] getArgs() {
        return this.args;
    }

    public boolean hasArgs() {
        return (this.args == null) ? false : true;
    }

    public boolean requiresArgs() {
        return false;
    }

    public void setSender(final OAPlayer sender) {
        this.sender = sender;
    }

    public void setArgs(String[] args) {
        try {
            this.args = args;
            this.power = Float.parseFloat(args[0]);
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            this.log.info(String.format("[%s] %s", e.getClass().getCanonicalName(), e.getMessage()));
            return;
        }
    }

    public void run(final OAPlayer player) {
        this.run(player.getWorld().getBlockAt(player.getLocation()));
    }

    public void run(final Block block) {
        this.target = block;
        this.t_location = block.getLocation();
        tracker.increment();
        OAExplosionListener.addOAOrigin(this.t_location);
        block.getLocation().getWorld().createExplosion(
            this.t_location, this.power, this.fire);
        this.sender.sendMessage(ChatColor.BLUE + String.format("%d blocks have been changed.", OAExplosionListener.getExplosion(this.t_location).size()));
        this.used = true;
    }

    public void run(final Entity entity) {
        if (entity instanceof Tameable) {
            if (((Tameable) entity).isTamed()) {
                this.sender.sendMessage(ChatColor.RED + "You can't blow up a tamed animal..that's horrible! :/");
                this.used = true;
                return;
            }
        }
        if (entity instanceof Wolf && ((Wolf) entity).isAngry() && ((((Wolf) entity).getTarget() instanceof Player)
            && (((Player) ((Wolf) entity).getTarget()).getName() == this.sender.getName())
            && this.acruelty == false)) {
            // its mad. i'll allow it.
        } else if (entity instanceof Animals && this.acruelty == false) {
           this.sender.sendMessage(ChatColor.RED + "Why would you hurt this poor little animal, you sick monster >:(");
           this.used = true;
           return;
        } else if (entity instanceof Golem && this.gcruelty == false) {
            this.sender.sendMessage(ChatColor.RED + "Why exactly do you want to kill a golem? (they help you, dummy) -_-'");
            this.used = true;
            return;
        }
        this.run(entity.getLocation().getWorld().getBlockAt(entity.getLocation()));
    }

    public void undo() {
        if (OAExplosionListener.hasExplosion(this.t_location)) {
            // cool, I can undo this explosion!
            List<BlockState> blocks = OAExplosionListener.getExplosion(this.t_location);
            Iterator blockstate_i = blocks.iterator();
            while (blockstate_i.hasNext()) {
                BlockState b = (BlockState) blockstate_i.next();
                // manually flush the blockstates back to the block forcibly. (and by manually, I mean not with b.update(true))
                b.getBlock().setTypeIdAndData(
                    b.getTypeId(), b.getRawData(), true);
            }
            // destroy remnants of the explosion.
            OAExplosionListener.purgeExplosion(this.t_location);
        } else {
            this.sender.sendMessage(ChatColor.RED + "I'm sorry, but I can't undo this explosion..");
            return;
        }
    }
}