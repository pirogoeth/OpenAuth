package me.maiome.openauth.actions;

// bukkit
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wolf;

// internal
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.bukkit.OAPlayer.Direction;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.*;

// java
import java.util.ArrayList;
import java.util.List;

public class HellHounds extends IBaseAction {

    public static final String name = "hounds";
    public static List<OAPlayer> attacking = new ArrayList<OAPlayer>();

    private Session attached;
    private SessionController sc;
    private final LogHandler log = new LogHandler();
    private final String permissible = "openauth.action.send-hounds";
    private final long attack_delay = ConfigInventory.MAIN.getConfig().getLong("actions.hounds.attack-delay", 60L);
    private final long removal = ConfigInventory.MAIN.getConfig().getLong("actions.hounds.removal-time", 600L);
    private OAServer server;
    private int destruction_taskid;
    private boolean used = false;

    protected OAPlayer sender;
    protected OAPlayer target;
    protected List<Wolf> spawned = new ArrayList<Wolf>();
    protected boolean storming;

    public HellHounds(OAServer server, Session attached) {
        this.server = server;
        this.sc = server.getController().getSessionController();
        this.attached = attached;
        this.setSender(this.attached.getPlayer());
    }

    // task for auto-destruction of wolves
    private Runnable destroy_wolves = new Runnable () {
        public void run() {
            stopAttacking();
        }
    }

    // task for creating the wolves and such.
    private Runnable attack = new Runnable () {
        public void run() {
            World world = ((Block) blocks.get(0)).getWorld();
            for (Block block : blocks) {
                world.strikeLightningEffect(block.getLocation());
                Wolf w = world.spawn(block.getLocation(), Wolf.class);
                spawned.append(w);
                w.setAngry(true);
                w.setTarget(target.getPlayer());
            }
        }
    }

    // action base methods
    public String getName() {
        return name;
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

    public boolean requiresArgs() {
        return false;
    }

    public String[] getArgs() {}

    public boolean hasArgs() {
        return false;
    }

    public void setArgs() {}

    /**
     * This method uses mine and TakSayu's directional placement algorithm and sk89q's direction detection
     * to place (maybe incendiary) wolves behind the player.
     */
    public List<Block> getApplicableBlocks(OAPlayer target) {
        List<Block> blocks = new ArrayList<Block>(2);
        Location loc = target.getLocation();
        switch (target.getSimpleDirection()) {
            case NORTH: // player is x-aligned, x++
                blocks.append(loc.getWorld().getBlockAt(
                    new Location(loc.getWorld(), loc.getBlockX() + 5, loc.getBlockY() + 1, loc.getBlockZ() + 3)
                ));
                blocks.append(loc.getWorld().getBlockAt(
                    new Location(loc.getWorld(), loc.getBlockX() + 5, loc.getBlockY() + 1, loc.getBlockZ() - 3)
                ));
                break;
            case SOUTH: // player is x-aligned, x--
                blocks.append(loc.getWorld().getBlockAt(
                    new Location(loc.getWorld(), loc.getBlockX() - 5, loc.getBlockY() + 1, loc.getBlockZ() + 3)
                ));
                blocks.append(loc.getWorld().getBlockAt(
                    new Location(loc.getWorld(), loc.getBlockX() - 5, loc.getBlockY() + 1, loc.getBlockZ() - 3)
                ));
                break;
            case EAST: // player is z-aligned, z++
                blocks.append(loc.getWorld().getBlockAt(
                    new Location(loc.getWorld(), loc.getBlockX() + 3, loc.getBlockY() + 1, loc.getBlockZ() + 5)
                ));
                blocks.append(loc.getWorld().getBlockAt(
                    new Location(loc.getWorld(), loc.getBlockX() - 3, loc.getBlockY() + 1, loc.getBlockZ() + 5)
                ));
                break;
            case WEST: // player is z-aligned, z--
                blocks.append(loc.getWorld().getBlockAt(
                    new Location(loc.getWorld(), loc.getBlockX() + 3, loc.getBlockY() + 1, loc.getBlockZ() - 5)
                ));
                blocks.append(loc.getWorld().getBlockAt(
                    new Location(loc.getWorld(), loc.getBlockX() - 3, loc.getBlockY() + 1, loc.getBlockZ() - 5)
                ));
                break;
        }
        return blocks;
    }

    // empty stubs for unused run blocks.
    public void run(Entity entity) {}
    public void run(Block block) {}

    // player attack method
    public void run(OAPlayer player) {
        if (attacking.contains(player)) {
            this.sender.sendMessage(ChatColor.BLUE + String.format("Player %s is already being attacked.", player.getName()));
            return;
        }
        this.target = player;
        attacking.append(player);
        this.storming = player.getLocation().getWorld().hasStorm();
        if (!(this.storming)) {
            player.getLocation().getWorld().setStorm(true);
            player.getLocation().getWorld().setThundering(true);
            player.getLocation().getWorld().setWeatherDuration((int) this.removal);
        }
        this.blocks = this.getApplicableBlocks(player);
        this.server.scheduleSyncDelayedTask(this.attack_delay, this.attack);
        this.destruction_taskid = this.server.scheduleSyncDelayedTask(this.removal, this.destroy_wolves);
    }

    public void undo() {} // stub for now.
}