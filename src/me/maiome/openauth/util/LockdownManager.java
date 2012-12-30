package me.maiome.openauth.util;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.*;

public class LockdownManager {

    public String lockdownMessage;
    public String defaultLockdownMessage;
    private OpenAuth server;
    private LogHandler log = new LogHandler();
    private boolean isLocked = false;
    private static LockdownManager instance = null;

    public LockdownManager() {
        this.lockdownMessage = this.defaultLockdownMessage = ConfigInventory.MAIN.getConfig().getString("server.lockdown-reason", "The server is currently locked down for maintenance.");
        instance = this;
    }

    public static LockdownManager getInstance() {
        if (instance == null) {
            instance = new LockdownManager();
        }
        return instance;
    }

    public boolean isLocked() {
        return this.isLocked;
    }

    public String getLockReason() {
        return this.lockdownMessage;
    }

    public String getDefaultLockReason() {
        return this.defaultLockdownMessage;
    }

    public void setLocked(boolean b) {
        this.isLocked = b;
    }

    public void toggleLock() {
        this.isLocked = !(this.isLocked);
    }

    public void setLockReason(String s) {
        this.lockdownMessage = s;
    }
}