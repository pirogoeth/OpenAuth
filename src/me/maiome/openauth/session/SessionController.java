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

    public SessionController (OpenAuth controller) {
        this.controller = controller;
        this.controller.scheduleAsynchronousRepeatingTask(300L, 1800L, new SessionPrunerTask(this));
    }

    public class SessionPrunerTask implements Runnable {

        final SessionController sc;

        public SessionPrunerTask(final SessionController sc) {
            this.sc = sc;
        }

        public void run () {
            List<OAPlayer> pruning = new ArrayList<OAPlayer>();

            for (Map.Entry<OAPlayer, Session> session_set : this.sc.session_bag.entrySet()) {
                if (!(session_set.getKey().getPlayer().isOnline())) {
                    pruning.add(session_set.getKey());
                }
            }

            Iterator prune = pruning.iterator();
            while (prune.hasNext()) {
                this.sc.forget(prune.next());
            }
        }
    }

    public Session create(OAPlayer player) {
        return new Session(this, player);
    }

    public Session create(Player player) {
        return new Session(this, this.controller.wrapOAPlayer(player));
    }

    public Session create(String player) {
        return new Session(this, this.controller.wrapOaPlayer(player));
    }

    public void remember(Session session) {
        this.session_bag.put(session.getPlayer(), session);
    }

    public void forget(Session session) {
        this.session_bag.remove(session.getPlayer());
    }

    public void forget(OAPlayer player) {
        this.session_bag.remove(player);
    }

    public Session get(OAPlayer player) {
        return this.session_bag.get(player);
    }

    public Session get(String player) {
        return this.session_bag.get(this.controller.wrapOAPlayer(player));
    }

    public Session get(Player player) {
        return this.session_bag.get(this.controller.wrapOAPlayer(player));
    }

