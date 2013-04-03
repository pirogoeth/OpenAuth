package me.maiome.openauth.actions;

// bukkit
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;

// internal
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.bukkit.OAPlayer.Direction;
import me.maiome.openauth.metrics.Tracker;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.*;

// java
import java.util.ArrayList;
import java.util.List;

public class HellHounds implements IAction {

    public static final String name = "hounds";
    public static final Tracker tracker = new Tracker("HellHounds");
    public static List<OAPlayer> attacking = new ArrayList<OAPlayer>();

    private final SessionController sc = SessionController.getInstance();
    private final LogHandler log = new LogHandler();
    private final String permissible = "openauth.action.fetch-me-their-souls";
    private final long attack_delay = Config.getConfig().getLong("actions.hounds.attack-delay", 60L);
    private final long removal_delay = Config.getConfig().getLong("actions.hounds.removal-delay", 600L);
    private final OAServer server = OAServer.getInstance();
    private Session attached;
    private int attack_taskid;
    private int destruction_taskid;
    private boolean used = false;

    protected OAPlayer sender;
    protected OAPlayer target;
    protected int target_mode;
    protected List<Block> blocks = new ArrayList<Block>(2);
    protected List<Wolf> spawned = new ArrayList<Wolf>();
    protected boolean storming;

    public HellHounds() { }

    public HellHounds(Session attached) {
        this.attached = attached;
        this.setSender(this.attached.getPlayer());
    }

    public String toString() {
        return String.format("HellHounds{permissible=%s}", this.permissible);
    }

    // task for auto-destruction of wolves
    private Runnable destroy_wolves = new Runnable () {
        public void run() {
            stopAttacking();
        }
    };

    // task for creating the wolves and such.
    private Runnable attack = new Runnable () {
        public void run() {
            World world = ((Block) blocks.get(0)).getWorld();
            for (Block block : blocks) {
                world.strikeLightningEffect(block.getLocation());
                Wolf w = world.spawn(block.getLocation(), Wolf.class);
                spawned.add(w);
                w.damage(0, (Entity) target.getPlayer());
                w.setNoDamageTicks((int) removal_delay);
                w.setFireTicks((int) removal_delay);
                w.setAngry(true);
            }
        }
    };

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

    // smash all of this into a smaller block instead of spanning it out. cleaner and shorter.
    public boolean requiresEntityTarget() { return true; }
    public boolean allowsAnyEntityTarget() { return false; }
    public boolean allowsArgs() { return false; }
    public boolean requiresArgs() { return false; }
    public boolean hasArgs() { return false; }
    public String[] getArgs() { return null; }

    public void setArgs(String[] args) {}

    public void setSender(final OAPlayer sender) {
        this.sender = sender;
    }

    /**
     * This method uses my and TakSayu's directional placement algorithm and sk89q's direction detection
     * to place (maybe incendiary) wolves behind the player.
     */
    public List<Block> getApplicableBlocks(OAPlayer target) {
        List<Block> blocks = new ArrayList<Block>(2);
        Location loc = target.getLocation();
        switch (target.getSimpleDirection()) {
            case NORTH: // player is x-aligned, x++
                blocks.add(loc.getWorld().getBlockAt(
                    new Location(loc.getWorld(), loc.getBlockX() + 5, loc.getBlockY() + 1, loc.getBlockZ() + 3)
                ));
                blocks.add(loc.getWorld().getBlockAt(
                    new Location(loc.getWorld(), loc.getBlockX() + 5, loc.getBlockY() + 1, loc.getBlockZ() - 3)
                ));
                break;
            case SOUTH: // player is x-aligned, x--
                blocks.add(loc.getWorld().getBlockAt(
                    new Location(loc.getWorld(), loc.getBlockX() - 5, loc.getBlockY() + 1, loc.getBlockZ() + 3)
                ));
                blocks.add(loc.getWorld().getBlockAt(
                    new Location(loc.getWorld(), loc.getBlockX() - 5, loc.getBlockY() + 1, loc.getBlockZ() - 3)
                ));
                break;
            case EAST: // player is z-aligned, z++
                blocks.add(loc.getWorld().getBlockAt(
                    new Location(loc.getWorld(), loc.getBlockX() + 3, loc.getBlockY() + 1, loc.getBlockZ() + 5)
                ));
                blocks.add(loc.getWorld().getBlockAt(
                    new Location(loc.getWorld(), loc.getBlockX() - 3, loc.getBlockY() + 1, loc.getBlockZ() + 5)
                ));
                break;
            case WEST: // player is z-aligned, z--
                blocks.add(loc.getWorld().getBlockAt(
                    new Location(loc.getWorld(), loc.getBlockX() + 3, loc.getBlockY() + 1, loc.getBlockZ() - 5)
                ));
                blocks.add(loc.getWorld().getBlockAt(
                    new Location(loc.getWorld(), loc.getBlockX() - 3, loc.getBlockY() + 1, loc.getBlockZ() - 5)
                ));
                break;
        }
        return blocks;
    }

    // attack stop method
    public void stopAttacking() {
        // first, alert the attackee
        this.target.sendMessage(ChatColor.BLUE + "You have survived the attack...good job!");
        // despawn the wolves
        for (Wolf w : this.spawned) {
            w.remove();
        }
        // reset the player's gamemode
        this.target.getPlayer().setGameMode(GameMode.getByValue(this.target_mode));
        // reset the weather
        if (this.storming) {
            this.target.getWorld().setStorm(true);
            this.target.getWorld().setThundering(false);
        } else {
            this.target.getWorld().setStorm(false);
        }
        attacking.remove(this.target);
    }

    // empty stubs for unused run blocks.
    public void run(Entity entity) {}
    public void run(Block block) {}

    // player attack method
    public void run(OAPlayer player) {
        if (attacking.contains(player)) {
            this.sender.sendMessage(ChatColor.BLUE + String.format("Player %s is already being attacked.", player.getName()));
            this.used = false;
            return;
        }
        tracker.increment();
        this.target = player;
        attacking.add(player);
        this.storming = player.getLocation().getWorld().hasStorm();
        if (!(this.storming)) {
            player.getLocation().getWorld().setStorm(true);
            player.getLocation().getWorld().setThundering(true);
            player.getLocation().getWorld().setWeatherDuration((int) this.removal_delay);
        }
        this.blocks = this.getApplicableBlocks(player);
        this.target.sendMessage(ChatColor.RED + "Beware the hounds...");
        this.target_mode = this.target.getPlayer().getGameMode().getValue();
        this.target.getPlayer().setGameMode(GameMode.SURVIVAL);
        this.server.scheduleSyncDelayedTask(this.attack_delay, this.attack);
        this.destruction_taskid = this.server.scheduleSyncDelayedTask(this.removal_delay, this.destroy_wolves);
        this.used = true;
    }

    public void undo() {
        if (this.used == true) {
            this.server.cancelTask(this.destruction_taskid);
            this.server.scheduleSyncDelayedTask(0L, this.destroy_wolves);
            this.sender.sendMessage(ChatColor.BLUE + String.format("Attack on %s cancelled.", this.target.getName()));
        } else {
            this.sender.sendMessage(ChatColor.BLUE + "This action cannot be undone, sorry o_o..");
        }
    }
}