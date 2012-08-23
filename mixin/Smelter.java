import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.*;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.mixins.*;
import me.maiome.openauth.util.*;

import java.util.*;

/**
 * This is a piston smelting mixin.
 * Inspired by feildmaster's PistonSmelter.
 */
public class Smelter implements IMixin, Listener {

    private final String name = "Smelter";
    private OpenAuth controller;
    private MixinManager mixinManager;
    private static final LogHandler log = new LogHandler();

    public Smelter() {
        this.controller = (OpenAuth) OpenAuth.getInstance();
    }

    public String getName() {
        return this.name;
    }

    public void onInit() {
        OpenAuth.getInstance().getServer().getPluginManager().registerEvents(this, OpenAuth.getInstance());
    }

    public void onTeardown() { }

    private boolean isOnPiston(Block block) {
        return block.getRelative(BlockFace.EAST).getType() == Material.PISTON_EXTENSION ||
               block.getRelative(BlockFace.WEST).getType() == Material.PISTON_EXTENSION ||
               block.getRelative(BlockFace.NORTH).getType() == Material.PISTON_EXTENSION ||
               block.getRelative(BlockFace.SOUTH).getType() == Material.PISTON_EXTENSION ||
               block.getRelative(BlockFace.UP).getType() == Material.PISTON_EXTENSION;
    }

    private void drop(Block block) {
        Material drop = null;
        if (block.getType() == Material.STONE) {
            drop = Material.STONE;
        } else if (block.getType() == Material.COBBLESTONE) {
            drop = Material.COBBLESTONE;
        } else if (block.getType() == Material.IRON_BLOCK) {
            drop = Material.IRON_INGOT;
        } else if (block.getType() == Material.GOLD_BLOCK) {
            drop = Material.GOLD_INGOT;
        } else if (block.getType() == Material.GLASS) {
            drop = Material.GLASS;
        } else if (block.getType() == Material.BRICK) {
            drop = Material.BRICK;
        } else {
            drop = block.getType();
        }
        if (drop == null) return;

        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(drop, 1, (short) 0, block.getData()));
        block.setType(Material.AIR);
    }

    private void smelt(Block block) {
        int id = -1;
        byte data = 127;


        if (block.getType() == Material.COBBLESTONE) {
            id = Material.STONE.getId();
        } else if (block.getType() == Material.IRON_ORE) {
            id = Material.IRON_BLOCK.getId();
        } else if (block.getType() == Material.GOLD_ORE) {
            id = Material.GOLD_BLOCK.getId();
        } else if (block.getType() == Material.SAND) {
            id = Material.GLASS.getId();
        } else if (block.getType() == Material.CLAY) {
            id = Material.BRICK.getId();
        }

        if (id != -1) {
            block.setTypeIdAndData(id, data, true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        Block under = block.getRelative(BlockFace.DOWN);
        Block base = under.getRelative(BlockFace.DOWN);

        if ((under.getType() == Material.LAVA || under.getType() == Material.STATIONARY_LAVA) || (under.getType() == Material.IRON_FENCE && (base.getType() == Material.LAVA || base.getType() == Material.STATIONARY_LAVA))) {
            if (this.isOnPiston(block)) {
                this.smelt(block);
            }
        } else if (under.getType() == Material.CAULDRON) {
            if (((Cauldron) under.getType().getNewData(under.getData())).isFull()) {
                this.drop(block);
            }
        } else if (under.getType() == Material.WATER && block.getData() == 127) {
            this.drop(block);
        }
    }
}