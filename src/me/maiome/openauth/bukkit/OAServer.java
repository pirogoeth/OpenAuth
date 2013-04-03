package me.maiome.openauth.bukkit;

// java imports
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

// bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

// internal imports
import me.maiome.openauth.bukkit.events.*;
import me.maiome.openauth.database.*;
import me.maiome.openauth.handlers.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.*;

public class OAServer extends Reloadable {

    private static OAServer instance;

    private final Server serverInst;
    private LogHandler log = new LogHandler();
    private OALoginHandler loginHandler;
    private OAWhitelistHandler whitelistHandler;
    private boolean startedTasks = false;

    // setup of fields for handlers
    private boolean wh_enabled;

    // time variables for scheduler tasks
    public long wlsave_delay;
    public long wlsave_period;

    // holds local-class task ids.
    private final List<Integer> taskIds = new ArrayList<Integer>();

    public static OAServer getInstance() {
        return ((instance != null) ? instance : new OAServer());
    }

    private final Runnable dbUserCleanerTask = new Runnable() {
        public void run() {
            if (serverInst.getOnlinePlayers().length != 0) {
                log.debug("[DB] Skipping user table pruning, server is not empty.");
                return;
            }
            synchronized (OpenAuth.databaseLock) {
                DBPlayer.clean();
            }
        }
    };

    public OAServer() {
        this.reload();
        this.serverInst = Bukkit.getServer();
        this.loginHandler = new OAActiveLoginHandler();
        this.whitelistHandler = new OAActiveWhitelistHandler();
        this.whitelistHandler.setEnabled(this.wh_enabled);
        this.setReloadable(this);

        instance = this;
    }

    public String toString() {
        return String.format("OAServer{wlsave_delay=%d,wlsave_period=%d}", this.wlsave_delay, this.wlsave_period);
    }

    protected void reload() {
        // cancel current local-class tasks
        for (int id : this.taskIds) {
            this.cancelTask(id);
        }
        this.taskIds.clear();
        // reload the configuration values
        this.wh_enabled = Config.getConfig().getBoolean("whitelist-handler", false);
        this.wlsave_delay = Config.getConfig().getLong("save-whitelist-delay", 2700L);
        this.wlsave_period = Config.getConfig().getLong("save-whitelist-period", 10800L);
        // reset the marker value
        this.startedTasks = false;
        // restart the scheduler tasks.
        this.startSchedulerTasks();
    }

    // scheduling

    public void startSchedulerTasks() {
        if (this.startedTasks == true) return;
        this.startedTasks = true;
        // runs scheduler tasks
        this.taskIds.add(this.scheduleAsynchronousRepeatingTask(this.wlsave_delay, this.wlsave_period, this.whitelistsaveTask));
        this.taskIds.add(this.scheduleAsynchronousRepeatingTask(12000L, 36000L, this.dbUserCleanerTask));
    }

    public int scheduleSyncRepetitiveTask(long delay, long period, Runnable task) {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(OpenAuth.getInstance(), task, delay, period);
    }

    public int scheduleSyncDelayedTask(long delay, Runnable task) {
        return Bukkit.getScheduler().scheduleSyncDelayedTask(OpenAuth.getInstance(), task, delay);
    }

    public int scheduleAsynchronousRepeatingTask(long delay, long period, Runnable task) {
        return Bukkit.getScheduler().scheduleAsyncRepeatingTask(OpenAuth.getInstance(), task, delay, period);
    }

    public void cancelTask(final int id) {
        Bukkit.getScheduler().cancelTask(id);
    }

    public void cancelAllOATasks() {
        Bukkit.getScheduler().cancelTasks(OpenAuth.getJavaPlugin());
    }

    // support

    public Server getServer() {
        return this.serverInst;
    }

    /**
     * Shorthand to register an event listener.
     */
    public void registerEvents(Listener listener) {
        this.serverInst.getPluginManager().registerEvents(listener, OpenAuth.getJavaPlugin());
    }

    /**
     * Shorthand to call an event.
     */
    public void callEvent(Event e) {
        this.serverInst.getPluginManager().callEvent(e);
    }

    // scheduled tasks.

    private Runnable whitelistsaveTask = new Runnable () {
        public void run() {
            whitelistHandler.saveWhitelist();
        }
    };

    // setup of handlers

    public OALoginHandler getLoginHandler() {
        return this.loginHandler;
    }

    public boolean isWHEnabled() {
        return this.wh_enabled;
    }

    public OAWhitelistHandler getWhitelistHandler() {
        return this.whitelistHandler;
    }

    // player-management methods

    public boolean banPlayer(final OAPlayer banned, final int type, final String banner, final String reason) {
        try {
            DBBanRecord record = new DBBanRecord(banned, type, banner, reason);
            OAPlayerBannedEvent event = new OAPlayerBannedEvent(banned, record);
            this.callEvent(event);
            if (event.isCancelled()) {
                record.delete();
                return false;
            }
            return true;
        } catch (java.lang.Exception e) {
            return false;
        }
    }

    public boolean unbanPlayer(final String name) {
        try {
            DBBanRecord record = OpenAuth.getInstance().getDatabase().find(DBBanRecord.class, name);
            OAPlayerUnbannedEvent event = new OAPlayerUnbannedEvent(name, record);
            this.callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
            record.delete();
            return true;
        } catch (java.lang.Exception e) {
            return false;
        }
    }

    public String getNameBanReason(final String name) {
        DBBanRecord record = OpenAuth.getInstance().getDatabase().find(DBBanRecord.class, name);
        try {
            if (record == null) {
                return "There has been an error in the ban system. Please contact the server administrator.";
            }
            return record.getReason();
        } catch (java.lang.Exception e) {
            return "There has been an error in the ban system. Please contact the server administrator.";
        }
    }

    public boolean hasNameBan(final String name) {
        DBBanRecord record = OpenAuth.getInstance().getDatabase().find(DBBanRecord.class, name);
        if (record == null) {
            return false;
        }
        if (record.getType() == 1 && record.getBannable().equals(name)) {
            return true;
        }
        return false;
    }

    public String getIPBanReason(final String IP) {
        List<DBBanRecord> records = OpenAuth.getInstance().getDatabase().find(DBBanRecord.class).where().eq("type", 2).eq("bannable", IP).findList();
        try {
            DBBanRecord record = records.get(0);
            if (record == null) {
                return "There has been an error in the ban system. Please contact the server administrator.";
            }
            return record.getReason();
        } catch (java.lang.Exception e) {
            return "There has been an error in the ban system. Please contact the server administrator.";
        }
    }

    public boolean hasIPBan(final String IP) {
        List<DBBanRecord> records = OpenAuth.getInstance().getDatabase().find(DBBanRecord.class).where().eq("type", 2).eq("bannable", IP).findList();
        try {
            DBBanRecord record = records.get(0);
            if (record == null) {
                return false;
            }
            return true;
        } catch (java.lang.Exception e) {
            return false;
        }
    }

    // whitelisting, login status methods

    public WhitelistStatus getPlayerWhitelistStatus(final OAPlayer player) {
        return this.whitelistHandler.getPlayerStatus(player);
    }

    public LoginStatus getPlayerLoginStatus(final OAPlayer player) {
        return this.loginHandler.getPlayerStatus(player);
    }
}

