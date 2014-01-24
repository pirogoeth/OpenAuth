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
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.util.*;

public class SessionController extends Reloadable {

    private Map<OAPlayer, Session> session_bag = new HashMap<OAPlayer, Session>();
    private LogHandler log = new LogHandler();
    private boolean started_tasks = false;
    private int taskId;
    private static SessionController instance = null;

    // task fields
    private long prune_delay;
    private long prune_period;
    private int prune_epsilon;

    public static SessionController getInstance() {
        if (instance == null) {
            new SessionController();
        }
        return instance;
    }

    public SessionController () {
        this.reload();
        this.setReloadable(this);
        instance = this;
    }

    protected void reload() {
        this.prune_delay = Config.getConfig().getLong("session-prune-delay", 6000L);
        this.prune_period = Config.getConfig().getLong("session-prune-period", 12000L);
        this.prune_epsilon = Config.getConfig().getInt("session-prune-epsilon", 3);
        OAServer.getInstance().cancelTask(this.taskId);
        this.started_tasks = false;
        this.startSchedulerTasks();
    }

    public String toString() {
        return String.format("SessionController{prune_delay=%d,prune_period=%d,prune_epsilon=%d}", this.prune_delay, this.prune_period, this.prune_epsilon);
    }

    public void startSchedulerTasks() {
        if (this.started_tasks == true) return;
        this.started_tasks = true;
        // run scheduler tasks
        this.taskId = OAServer.getInstance().scheduleAsynchronousRepeatingTask(this.prune_delay, this.prune_period, this.pruning_task);
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

            log.debug(String.format("Pruned %d sessions.", pruning.size()));
        }
    };

    public void createAll() {
        int session_count = 0;
        for (Player player : OAServer.getInstance().getServer().getOnlinePlayers()) {
            OAPlayer.getPlayer(player);
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
        if (players.size() >= 1) log.debug(String.format("[SessionController] Destroyed %d sessions.", players.size()));
    }

    public Session create(OAPlayer player) {
        Session session = new Session(player);
        this.remember(session);
        return session;
    }

    public Session create(Player player) {
        Session session = new Session(OAPlayer.getPlayer(player));
        this.remember(session);
        return session;
    }

    public Session create(String player) {
        Session session = new Session(OAPlayer.getPlayer(player));
        this.remember(session);
        return session;
    }

    public Session createTemp(OAPlayer player) {
        return new Session(player);
    }

    public void remember(Session session) {
        this.session_bag.put(session.getPlayer(), session);
    }

    private void _forget(Session session) {
        this.session_bag.remove(session.getPlayer());
    }

    private void _forget(OAPlayer player) {
        this.session_bag.remove(player);
    }

    public void forget(Object session) {
        if (session instanceof Session) {
            this._forget((Session) session);
        } else if (session instanceof OAPlayer) {
            this._forget((OAPlayer) session);
        } else if (session instanceof Player) {
            this._forget(OAPlayer.getPlayer((Player) session));
        }
    }

    public Session get(OAPlayer player) {
        if (this.session_bag.containsKey(player)) {
            return this.session_bag.get(player);
        } else {
            log.debug(String.format("[SessionController] Creating session for OAPlayer %s.", player.getName()));
            return this.create(player);
        }
    }

    public Session get(String player) {
        OAPlayer _player = OAPlayer.getPlayer(player);
        return this.get(_player);
    }

    public Session get(Player player) {
        OAPlayer _player = OAPlayer.getPlayer(player);
        return this.get(_player);
    }
}