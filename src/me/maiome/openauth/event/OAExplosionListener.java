package me.maiome.openauth.event;

// bukkit imports
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Event.Result;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.Location;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;

// java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OAExplosionListener implements Listener {

    private final OpenAuth controller;
    public static Map<Location, List<BlockState>> explosions = new HashMap<Location, List<BlockState>>();

    public OAExplosionListener(OpenAuth controller) {
        this.controller = controller;
    }

    public static List<BlockState> getExplosion(Location loc) {
        return explosions.get(loc);
    }
    public static Map<Location, List<BlockState>> getExplosions() {
        return explosions;
    }

    public static boolean hasExplosion(Location loc) {
        return explosions.containsKey(loc);
    }

    public static void purgeExplosion(Location loc) {
        explosions.remove(loc);
    }

    public static void purgeExplosions() {
        explosions.clear();
    }

    /**
     * This will watch for intently for explosions and provide a SAFE
     * and FAST way to undo said explosion from the BoomStick action.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        // heres our list for the block states
        List<BlockState> blockstates = new ArrayList<BlockState>();
        // so first, we're going to gather the block list;
        List<Block> blocks = event.blockList();
         // now, we're going to harvest blockstates before the event completes
        Iterator block_i = blocks.iterator();
        while (block_i.hasNext()) {
            // write each blocks blockstate to the new arraylist
            blockstates.add((BlockState) ((Block) block_i.next()).getState());
        }
        // we have our blockstates. now, lets put this explosion in the map
        explosions.put(event.getLocation(), blockstates);
    }
}