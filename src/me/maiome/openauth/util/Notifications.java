package me.maiome.openauth.util;

// java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// internal imports
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.ConfigInventory;

public class Notifications {

    /**
     * This class is basically going to queue notifications about blocked actions and such, basically to prevent spamming the user's
     * chat window too terribly much.
     */

    private static List<OAPlayer> cooling = new ArrayList<OAPlayer>();
    private static Map<OAPlayer, List<String>> n_queue = new HashMap<OAPlayer, List<String>>();
    private static final long lock_timeout = 100L;
    private static OAServer server;

    public Notifications(OAServer server) {
        this.server = server;
        this.server.scheduleAsynchronousRepeatingTask(6000L, 300L, this.flushq);
    }

    private Runnable flushq = new Runnable () {
        public void run() {
            Map<OAPlayer, List<String>> q = new HashMap<OAPlayer, List<String>>();
            List<String> ns;
            for (Map.Entry<OAPlayer, List<String>> entry : n_queue.entrySet()) {
                if (cooling.contains((OAPlayer) entry.getKey())) continue;
                ns = (List<String>) entry.getValue();
                if (ns.size() == 0) continue;
                sendMessage(entry.getKey(), ns.get(0));
                ns.remove(0);
                q.put(entry.getKey(), ns);
            }
            n_queue.clear();
            n_queue.putAll(q);
        }
    };

    private static void lock(final OAPlayer player) {
        server.scheduleSyncDelayedTask(
            lock_timeout,
            new Runnable() {
                public void run() {
                    unlock(player);
                }
            }
        );
    }

    private static void unlock(final OAPlayer player) {
        cooling.remove(player);
    }

    public static void sendMessage(OAPlayer player, String message) {
        if (!(cooling.contains(player))) {
            player.sendMessage(message);
            lock(player);
        } else {
            if (!(n_queue.containsKey(player))) {
                n_queue.put(player, new ArrayList<String>());
            }
            List<String> ns = n_queue.get(player);
            if (!(ns.contains(message))) { // filter dupe messages
                ns.add(message);
            }
            n_queue.put(player, ns);
        }
        return;
    }
}