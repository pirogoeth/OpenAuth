package me.maiome.openauth.session;

// java imports
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

// bukkit imports
import org.bukkit.entity.Player;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.session.Session;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.Permission;

public class SessionController {

    private Map<OAPlayer, Session> session_bag = new HashMap<OAPlayer, Session>();
    private LogHandler log = new LogHandler();
    private OpenAuth controller;
    private OAServer server = OpenAuth.getOAServer();
    private boolean started_tasks = false;

    // task fields
    private final long prune_delay = ConfigInventory.MAIN.getConfig().getLong("session-prune-delay", 6000L);
    private final long prune_period = ConfigInventory.MAIN.getConfig().getLong("session-prune-period", 12000L);
    private final int prune_epsilon = ConfigInventory.MAIN.getConfig().getInt("session-prune-epsilon", 3);

    public SessionController (OpenAuth controller) {
        this.controller = controller;
        log.exDebug(String.format("Session Pruning: {DELAY: %s, PERIOD: %s, EPSILON: %s}",
            Long.toString(prune_delay), Long.toString(prune_period), Integer.toString(prune_epsilon)));

        // register the SessionController instance.
        OpenAuth.setSessionController(this);
    }

    public void startSchedulerTasks() {
        if (this.started_tasks == true) return;
        this.started_tasks = true;
        // run scheduler tasks
        this.server.scheduleAsynchronousRepeatingTask(this.prune_delay, this.prune_period, this.pruning_task);
    }

    // scheduler tasks
    private Runnable pruning_task = new Runnable () {
        public void run () {

            List<OAPlayer> pruning = new ArrayList<OAPlayer>();

            for (Map.Entry<OAPlayer, Session> session_set : session_bag.entrySet()) {
                if (!(session_set.getKey().getPlayer().isOnline())) {
                    pruning.add(session_set.getKey());
                }
            }

            if (pruning.size() < prune_epsilon) {
                return;
            }

            Iterator pruner = pruning.iterator();
            while (pruner.hasNext()) {
                forget(pruner.next());
            }

            log.exDebug(String.format("Pruned %d sessions.", pruning.size()));
        }
    };

    public OpenAuth getController() {
        return this.controller;
    }

    public void createAll() {
        int session_count = 0;
        for (Player player : this.server.getServer().getOnlinePlayers()) {
            this.controller.wrap(player).initSession();
            session_count++;
        }
        if (session_count >= 1) log.info(String.format("[SessionController] Generated %d sessions.", session_count));
    }

    public void destroyAll() {
        List<OAPlayer> players = new ArrayList<OAPlayer>();
        for (Map.Entry<OAPlayer, Session> entry : this.session_bag.entrySet()) {
            players.add((OAPlayer) entry.getKey());
        }
        for (OAPlayer player : players) {
            player.destroySession();
        }
        if (players.size() >= 1) log.exDebug(String.format("Destroyed %d sessions.", players.size()));
    }

    private Session create(OAPlayer player) {
        Session session = new Session(this, player);
        this.remember(session);
        return session;
    }

    private Session create(Player player) {
        Session session = new Session(this, this.controller.wrap(player));
        this.remember(session);
        return session;
    }

    private Session create(String player) {
        Session session = new Session(this, this.controller.wrap(player));
        this.remember(session);
        return session;
    }

    public Session createTemp(OAPlayer player) {
        return new Session(this, player);
    }

    public void remember(Session session) {
        this.session_bag.put(session.getPlayer(), session);
    }

    private void _forget(Session session) {
        this.session_bag.remove(session.getPlayer());
    }

    public void forget(Object session) {
        if (session instanceof Session) {
            this._forget((Session) session);
        } else if (session instanceof OAPlayer) {
            this._forget((Session) ((OAPlayer) session).getSession());
        } else if (session instanceof Player) {
            this._forget(this.get((Player) session));
        }
    }

    public Session get(OAPlayer player) {
        if (this.session_bag.containsKey(player)) {
            return this.session_bag.get(player);
        } else {
            log.exDebug(String.format("Creating session for OAPlayer %s.", player.getName()));
            return this.create(player);
        }
    }

    public Session get(String player) {
        OAPlayer _player = this.controller.wrap(player);
        return this.get(_player);
    }

    public Session get(Player player) {
        OAPlayer _player = this.controller.wrap(player);
        return this.get(_player);
    }
}