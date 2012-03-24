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

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.handlers.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.Permission;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.LoginStatus;
import me.maiome.openauth.util.WhitelistStatus;

public class OAServer {

    private OpenAuth controller;
    private Server server;
    private LogHandler log = new LogHandler();
    private OALoginHandler loginHandler;
    private boolean started_tasks = false;

    // ban containers
    private Map<String, Object> ip_bans = new HashMap<String, Object>();
    private Map<String, Object> name_bans = new HashMap<String, Object>();

    // setup of fields for handlers
    private final boolean lh_enabled = (ConfigInventory.MAIN.getConfig().getString("login-handler", "off").equals("off")) ? false : true;
    private final boolean wh_enabled = (ConfigInventory.MAIN.getConfig().getString("whitelist-handler", "off").equals("off")) ? false : true;
    private final boolean lh_extendable = (ConfigInventory.MAIN.getConfig().getString("login-handler", "default").equals("extended")) ? true : false;
    private final boolean wh_extendable = (ConfigInventory.MAIN.getConfig().getString("whitelist-handler", "default").equals("extended")) ? true : false;

    // time variables for scheduler tasks
    public final long autosave_delay = ConfigInventory.MAIN.getConfig().getLong("save-ban-delay", 900L);
    public final long autosave_period = ConfigInventory.MAIN.getConfig().getLong("save-ban-period", 5400L);

    public OAServer(OpenAuth controller, Server server) {
        this.controller = controller;
        this.server = server;
        this.loginHandler = new OAActiveLoginHandler(this.controller);
        log.exDebug(String.format("AutoSave: {DELAY: %s, PERIOD: %S}", Long.toString(autosave_delay), Long.toString(autosave_period)));
        log.exDebug(String.format("LoginHandler: {ENABLED: %s, EXTENDABLE: %s}", Boolean.toString(lh_enabled), Boolean.toString(lh_extendable)));
        log.exDebug(String.format("WhitelistHandler: {ENABLED: %s, EXTENDABLE: %s}", Boolean.toString(wh_enabled), Boolean.toString(wh_extendable)));
    }

    // scheduling

    public void startSchedulerTasks() {
        if (this.started_tasks == true) return;
        this.started_tasks = true;
        // runs scheduler tasks
        this.scheduleAsynchronousRepeatingTask(this.autosave_delay, this.autosave_period, this.autosave_task);
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

    // support

    public Server getServer() {
        return this.server;
    }

    public OpenAuth getController() {
        return this.controller;
    }

    public SessionController getSessionController() {
        return this.controller.getSessionController();
    }

    public Map<String, Object> getNameBans() {
        return new HashMap<String, Object>(this.name_bans);
    }

    public Map<String, Object> getIPBans() {
        return new HashMap<String, Object>(this.ip_bans);
    }

    // scheduled tasks.

    private Runnable autosave_task = new Runnable () {
        public void run() {
            saveBans();
        }
    };

    // setup of handlers

    public boolean isLHExtendable() {
        return this.lh_extendable;
    }

    public boolean isLHEnabled() {
        return this.lh_enabled;
    }

    public void setLoginHandler(OALoginHandler lh) {
        this.loginHandler = lh;
    }

    public OALoginHandler getLoginHandler() {
        return this.loginHandler;
    }

    public boolean isWHExtendable() {
        return this.wh_extendable;
    }

    public boolean isWHEnabled() {
        return this.wh_enabled;
    }

    // player-management methods

    public void kickPlayer(final OAPlayer player) {
        player.getPlayer().kickPlayer("No reason.");
        log.info("Kicked player: " + player.getName());
        player.setOffline();
    }

    public void kickPlayer(final OAPlayer player, final String reason) {
        player.getPlayer().kickPlayer(reason);
        log.info("Kicked player: " + player.getName() + ", reason: " + reason);
        player.setOffline();
    }

    public void banPlayerByIP(final OAPlayer player) {
        if (!(this.ip_bans.containsKey(player.getIP().replace('.', ',')))) {
            this.ip_bans.put(player.getIP().replace('.', ','), "No reason given.");
        }
    }

    public void banPlayerByIP(final OAPlayer player, final String reason) {
        if (!(this.ip_bans.containsKey(player.getIP().replace('.', ',')))) {
            this.ip_bans.put(player.getIP().replace('.', ','), reason);
        }
    }

    public void banPlayerByIP(final String IP) {
        if (!(this.ip_bans.containsKey(IP.replace('.', ',')))) {
            this.ip_bans.put(IP.replace('.', ','), "No reason given.");
        }
    }

    public void banPlayerByIP(final String IP, final String reason) {
        if (!(this.ip_bans.containsKey(IP.replace('.', ',')))) {
            this.ip_bans.put(IP.replace('.', ','), reason);
        }
    }

    public void unbanPlayerByIP(final OAPlayer player) {
        if (this.ip_bans.containsKey(player.getIP().replace('.', ','))) {
            this.ip_bans.remove(player.getIP().replace('.', ','));
            log.info(String.format("Removed ban for %s(%s).", player.getName(), player.getIP()));
        }
    }

    public void unbanPlayerByIP(final String IP) {
        if (this.ip_bans.containsKey(IP.replace('.', ','))) {
            this.ip_bans.remove(IP.replace('.', ','));
            log.info(String.format("Removed ban for %s.", IP));
        }
    }

    public void banPlayerByName(final OAPlayer player) {
        if (!(this.name_bans.containsKey(player.getName()))) {
            this.name_bans.put(player.getName(), "No reason given.");
        }
    }

    public void banPlayerByName(final OAPlayer player, final String reason) {
        if (!(this.name_bans.containsKey(player.getName()))) {
            this.name_bans.put(player.getName(), reason);
        }
    }

    public void banPlayerByName(final String player) {
        if (!(this.name_bans.containsKey(player))) {
            this.name_bans.put(player, "No reason given.");
        }
    }

    public void banPlayerByName(final String player, final String reason) {
        if (!(this.name_bans.containsKey(player))) {
            this.name_bans.put(player, reason);
        }
    }

    public void unbanPlayerByName(final OAPlayer player) {
        if (this.name_bans.containsKey(player.getName())) {
            this.name_bans.remove(player.getName());
            log.info(String.format("Removed ban for %s.", player.getName()));
        }
    }

    public void unbanPlayerByName(final String player) {
        if (this.name_bans.containsKey(player)) {
            this.name_bans.remove(player);
            log.info(String.format("Removed ban for %s.", player));
        }
    }

    public boolean hasNameBan(final String name) {
        return this.name_bans.containsKey(name);
    }

    public String getNameBanReason(final String name) {
        return (String) this.name_bans.get(name);
    }

    public boolean hasIPBan(final String IP) {
        return this.ip_bans.containsKey(IP.replace('.', ','));
    }

    public String getIPBanReason(final String IP) {
        return (String) this.ip_bans.get(IP.replace('.', ','));
    }

    public void saveBans() {
        try {
            ConfigInventory.DATA.getConfig().set("ban_storage.ip", this.ip_bans);
            ConfigInventory.DATA.getConfig().set("ban_storage.name", this.name_bans);
            log.info("Successfully saved bans.");
        } catch (java.lang.Exception e) {
            log.exDebug("Exception occurred while saving bans (this could just mean you have no bans to save).");
            log.exDebug(e.getMessage());
            return;
        }
    }

    public void loadBans() {
        try {
            this.ip_bans = (Map<String, Object>) ConfigInventory.DATA.getConfig().getConfigurationSection("ban_storage.ip").getValues(true);
            this.name_bans = (Map<String, Object>) ConfigInventory.DATA.getConfig().getConfigurationSection("ban_storage.name").getValues(true);
        } catch (java.lang.Exception e) {
            log.exDebug("Exception occurred while loading bans (this could just mean you have no bans to load).");
            log.exDebug(e.getMessage());
            return;
        }
    }

    // whitelisting, login status methods

    public WhitelistStatus getPlayerWhitelistStatus(final OAPlayer player) {
        // return this.whitelistHandler.getPlayerStatus(player);
        return WhitelistStatus.UNKNOWN;
    }

    public LoginStatus getPlayerLoginStatus(final OAPlayer player) {
        return this.loginHandler.getPlayerStatus(player);
    }
}

