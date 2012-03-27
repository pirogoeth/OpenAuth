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
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;

// java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OAExplosionListener implements Listener {

    private final OpenAuth controller;
    private final LogHandler log = new LogHandler();
    private static List<Location> oa_origin = new ArrayList<Location>();
    private static List<Location> watching = new ArrayList<Location>();
    private static Map<Location, List<BlockState>> explosions = new HashMap<Location, List<BlockState>>();
    private final float power = (float) ConfigInventory.MAIN.getConfig().getDouble("actions.boom.power", 2.0D);
    private final boolean fire = ConfigInventory.MAIN.getConfig().getBoolean("actions.boom.fire", false);

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

    public static void addWatching(Location loc) {
        watching.add(loc);
    }

    public static boolean isWatching(Location loc) {
        return watching.contains(loc);
    }

    public static void stopWatching(Location loc) {
        watching.remove(loc);
    }

    public static void clearWatching() {
        watching.clear();
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
     * This will watch for intently for explosions and provide a SAFE
     * and FAST way to undo said explosion from the BoomStick action.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        // debugging message
        log.exDebug(String.format("Explosion at {%s, %f, %f, %f}.",
            event.getLocation().getWorld().getName(), event.getLocation().getX(), event.getLocation().getY(), event.getLocation().getZ()));
        // heres our list for the block states
        List<BlockState> blockstates = new ArrayList<BlockState>();
        // check if we need to watch for this event to be called again
        if (!(isWatching(event.getLocation())) && hasOAOrigin(event.getLocation())) {
            log.exDebug("a");
            // cancel the event
            event.setCancelled(true);
            addWatching(event.getLocation()); // watch the location for another event
            removeOAOrigin(event.getLocation()); // remove the location from the origin indicator
        } else if (isWatching(event.getLocation())) {
            log.exDebug("b");
            stopWatching(event.getLocation());
            return;
        } else if (!(hasOAOrigin(event.getLocation()))) {
            return;
        }
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
        // restart the explosion.
        event.getLocation().getWorld().createExplosion(event.getLocation(), this.power, this.fire);
    }
}