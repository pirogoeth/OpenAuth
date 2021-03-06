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
import me.maiome.openauth.util.LogHandler;

// java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public class OAExplosionListener implements Listener {

    private static List<Location> oa_origin = new ArrayList<Location>();
    private static Map<Location, List<BlockState>> explosions = new HashMap<Location, List<BlockState>>();

    public OAExplosionListener() {
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

    public static void addOAOrigin(Location loc) {
        oa_origin.add(loc);
    }

    public static void removeOAOrigin(Location loc) {
        oa_origin.remove(loc);
    }

    public static boolean hasOAOrigin(Location loc) {
        return oa_origin.contains(loc);
    }

    /**
     * This will watch for explosions and provide a SAFE
     * and FAST way to undo said explosion from the BoomStick action.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        // check if we need to watch for this event to be called again
        if (hasOAOrigin(event.getLocation()) && !(event.isCancelled())) {
            removeOAOrigin(event.getLocation()); // remove the location from the origin indicator
        } else if (!(hasOAOrigin(event.getLocation()))) {
            return;
        }
        // heres our list for the block states
        List<BlockState> blockstates = new ArrayList<BlockState>();
        // now, we're going to harvest blockstates before the event completes
        for (Block block : event.blockList()) {
            // write each blocks blockstate to the new arraylist
            blockstates.add(block.getState());
        }
        // reverse the blockstates
        Collections.reverse(blockstates);
        // we have our blockstates. now, lets put this explosion in the map
        explosions.put(event.getLocation(), blockstates);
        // finally, set the explosion yield
        event.setYield(0F);
    }
}
