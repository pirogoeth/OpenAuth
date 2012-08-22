package me.maiome.openauth.bukkit;

// java imports
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.net.InetAddress;
import java.net.NetworkInterface;

// bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

// internal imports
import me.maiome.openauth.bukkit.events.*;
import me.maiome.openauth.database.*;
import me.maiome.openauth.handlers.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.Permission;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.LoginStatus;
import me.maiome.openauth.util.WhitelistStatus;

public class OAServer {

    protected final int factor = (17 * 5);
    protected final int serial = 100;

    private OpenAuth controller;
    private Server server;
    private LogHandler log = new LogHandler();
    private OALoginHandler loginHandler;
    private OAWhitelistHandler whitelistHandler;
    private boolean started_tasks = false;

    // setup of fields for handlers
    private final boolean wh_enabled = ConfigInventory.MAIN.getConfig().getBoolean("whitelist-handler", false);

    // time variables for scheduler tasks
    public final long wlsave_delay = ConfigInventory.MAIN.getConfig().getLong("save-whitelist-delay", 2700L);
    public final long wlsave_period = ConfigInventory.MAIN.getConfig().getLong("save-whitelist-period", 10800L);

    public OAServer(OpenAuth controller, Server server) {
        this.controller = controller;
        this.server = server;
        this.loginHandler = new OAActiveLoginHandler(this.controller);
        this.whitelistHandler = new OAActiveWhitelistHandler(this.controller);
        this.loginHandler.setEnabled(true);
        this.whitelistHandler.setEnabled(this.wh_enabled);
        // debugging information
        log.exDebug(String.format("WhitelistSave: {DELAY: %s, PERIOD: %s}", Long.toString(wlsave_delay), Long.toString(wlsave_period)));
        log.exDebug(String.format("WhitelistHandler: {ENABLED: %s}", Boolean.toString(wh_enabled)));

        // register the OAServer instance.
        OpenAuth.setOAServer(this);
    }

    public String toString() {
        return String.format("OAServer{wlsave_delay=%d,wlsave_period=%d}", this.wlsave_delay, this.wlsave_period);
    }

    public int hashCode() {
        return (int) Math.abs(((this.factor) + (this.controller.hashCode() + this.server.hashCode() + this.serial)));
    }

    // scheduling

    public void startSchedulerTasks() {
        if (this.started_tasks == true) return;
        this.started_tasks = true;
        // runs scheduler tasks
        this.scheduleAsynchronousRepeatingTask(this.wlsave_delay, this.wlsave_period, this.whitelistsave_task);
    }

    public int scheduleSyncRepetitiveTask(long delay, long period, Runnable task) {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(this.controller, task, delay, period);
    }

    public int scheduleSyncDelayedTask(long delay, Runnable task) {
        return Bukkit.getScheduler().scheduleSyncDelayedTask(this.controller, task, delay);
    }

    public int scheduleAsynchronousRepeatingTask(long delay, long period, Runnable task) {
        return Bukkit.getScheduler().scheduleAsyncRepeatingTask(this.controller, task, delay, period);
    }

    public void cancelTask(final int id) {
        Bukkit.getScheduler().cancelTask(id);
    }

    public void cancelAllOATasks() {
        Bukkit.getScheduler().cancelTasks(this.controller);
    }

    // support

    public Server getServer() {
        return this.server;
    }

    public OpenAuth getController() {
        return this.controller;
    }

    public void callEvent(Event e) {
        this.server.getPluginManager().callEvent(e);
    }

    public SessionController getSessionController() {
        return this.controller.getSessionController();
    }

    // scheduled tasks.

    private Runnable whitelistsave_task = new Runnable () {
        public void run() {
            whitelistHandler.saveWhitelist();
        }
    };

    // setup of handlers

    public void setLoginHandler(OALoginHandler lh) {
        this.loginHandler = lh;
    }

    public OALoginHandler getLoginHandler() {
        return this.loginHandler;
    }

    public boolean isWHEnabled() {
        return this.wh_enabled;
    }

    public void setWhitelistHandler(OAWhitelistHandler wh) {
        this.whitelistHandler = wh;
    }

    public OAWhitelistHandler getWhitelistHandler() {
        return this.whitelistHandler;
    }

    // player-management methods

    public void kickPlayer(OAPlayer player) {
        player.kickPlayer("No reason.");
        log.info("Kicked player: " + player.getName());
        player.setOffline();
    }

    public void kickPlayer(OAPlayer player, final String reason) {
        player.getPlayer().kickPlayer(reason);
        log.info("Kicked player: " + player.getName() + ", reason: " + reason);
        player.setOffline();
    }

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

