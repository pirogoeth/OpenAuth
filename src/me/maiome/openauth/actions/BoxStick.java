package me.maiome.openauth.actions;

// bukkit imports
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

// internal
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.metrics.Tracker;
import me.maiome.openauth.util.*;

// java
import java.util.ArrayList;
import java.util.List;

public class BoxStick implements IAction {

    protected final int factor = (17 * 7);
    protected final int serial = 304;

    public static String name = "box";
    public static final Tracker tracker = new Tracker("BoxStick");

    private String[] args = null;
    private Session attached;
    private SessionController sc;
    private final LogHandler log = new LogHandler();
    private final String permissible = "openauth.action.box";
    private OAServer server;
    private boolean used = false;
    private int box_material = ConfigInventory.MAIN.getConfig().getInt("actions.box.material-id", 1);
    private boolean add_torch = ConfigInventory.MAIN.getConfig().getBoolean("actions.box.torch", true);

    protected OAPlayer sender;
    protected LivingEntity target;
    protected List<BlockState> prev_states = new ArrayList<BlockState>();
    protected List<Block> blocks = new ArrayList<Block>();

    public BoxStick(OAServer server, Session attached) {
        this.server = server;
        this.sc = OpenAuth.getSessionController();
        this.attached = attached;
        this.setSender(this.attached.getPlayer());
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return String.format("BoxStick{permissible=%s}", this.permissible);
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

    public void setArgs(final String[] args) {
        try {
            this.args = args;
            this.box_material = Integer.valueOf(args[0]);
        } catch (java.lang.Exception e) {
            this.log.info(String.format("[%s] %s", e.getClass().getCanonicalName(), e.getMessage()));
            return;
        }
    }

    public void run(final Block block) {};

    public void run(final OAPlayer player) {
        this.run((LivingEntity) player.getPlayer());
    }

    public void run(final Entity entity) {
        tracker.increment();
        Location loc = entity.getLocation();
        World world = loc.getWorld();
        this.blocks.add(world.getBlockAt(loc.subtract(0, 1, 0))); // block under entity's feet
        this.blocks.add(world.getBlockAt(loc.add(1, 1, 0))); // +x, =y, =z
        this.blocks.add(world.getBlockAt(loc.subtract(1, 1, 0))); // -x, =y, =z
        this.blocks.add(world.getBlockAt(loc.add(0, 1, 1))); // =x, =y, +z
        this.blocks.add(world.getBlockAt(loc.subtract(0, 1, 1))); // =x, =y, -z
        this.blocks.add(world.getBlockAt(loc.add(1, 2, 0))); // +x, =y, =z
        this.blocks.add(world.getBlockAt(loc.subtract(1, 2, 0))); // -x, =y, =z
        this.blocks.add(world.getBlockAt(loc.add(0, 2, 1))); // =x, =y, +z
        this.blocks.add(world.getBlockAt(loc.subtract(0, 2, 1))); // =x, =y, -z
        for (Block block : this.blocks) {
            prev_states.add(block.getState());
            BlockState bs = block.getState();
            bs.setTypeId(this.box_material);
            bs.update(true);
        }
        if (this.add_torch) {
            prev_states.add(world.getBlockAt(loc.add(0, 2, 0)).getState());
            world.getBlockAt(loc.add(0, 2, 0)).setTypeId(89);
        } else {
            prev_states.add(world.getBlockAt(loc.add(0, 2, 0)).getState());
            world.getBlockAt(loc.add(0, 2, 0)).setTypeId(this.box_material);
        }
    }

    public void undo() {
        for (BlockState state : prev_states) {
            state.update(true);
        }
    }
}