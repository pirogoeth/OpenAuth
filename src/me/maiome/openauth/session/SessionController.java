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
    private OAServer server;
    private Runnable pruning_task = new Runnable () {
        public void run () {

            List<OAPlayer> pruning = new ArrayList<OAPlayer>();

            for (Map.Entry<OAPlayer, Session> session_set : session_bag.entrySet()) {
                if (!(session_set.getKey().getPlayer().isOnline())) {
                    pruning.add(session_set.getKey());
                }
            }

            Iterator prune = pruning.iterator();
            while (prune.hasNext()) {
                forget(prune.next());
            }
        }
    };

    public SessionController (OpenAuth controller) {
        this.controller = controller;
        this.server = this.controller.getOAServer();
        this.server.scheduleAsynchronousRepeatingTask(900L, 1800L, this.pruning_task);
    }

    public OpenAuth getController() {
        return this.controller;
    }

    private Session create(OAPlayer player) {
        Session session = new Session(this, player);
        this.remember(session);
        return session;
    }

    private Session create(Player player) {
        Session session = new Session(this, this.controller.wrapOAPlayer(player));
        this.remember(session);
        return session;
    }

    private Session create(String player) {
        Session session = new Session(this, this.controller.wrapOAPlayer(player));
        this.remember(session);
        return session;
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

    public void forget(Object session) {
        this.forget((Session) session);
    }

    public Session get(OAPlayer player) {
        if (this.session_bag.contrainsKey(player))
            return this.session_bag.get(player);
        else
            return this.create(player);
    }

    public Session get(String player) {
        OAPlayer _player = this.controller.wrapOAPlayer(player);
        if (this.session_bag.containsKey(_player))
            return this.session_bag.get(_player);
        else
            return this.create(_player);
    }

    public Session get(Player player) {
        OAPlayer _player = this.controller.wrapOAPlayer(player);
        if (this.session_bag.containsKey(_player)
            return this.session_bag.get(_player);
        else
            return this.create(_player);
    }
}