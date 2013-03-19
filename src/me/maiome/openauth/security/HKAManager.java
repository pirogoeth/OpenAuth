package me.maiome.openauth.security;

import java.util.*;

import com.avaje.ebean.*;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.database.*;
import me.maiome.openauth.util.*;

public class HKAManager extends Reloadable {

    private boolean hkAllowed;
    private String hkBaseDomain;

    private static HKAManager instance;

    /**
     * Returns the HKAManager instance.
     */
    public static HKAManager getInstance() {
        return ((instance != null) ? instance : new HKAManager());
    }

    private HKAManager() {
        this.reload();
        instance = this;
    }

    /**
     * Implementation of Reloadable; also loads the values when instantiated.
     */
    protected void reload() {
        this.hkAllowed = ConfigInventory.MAIN.getConfig().getBoolean("hkauth.allowed", false);
        this.hkBaseDomain = ConfigInventory.MAIN.getConfig().getString("hkauth.basedomain", null);
    }

    /**
     * Random key generation algorithm. At a 24 character length, there were no collisions with 100000 iterations of the function.
     */
    private String generateHKey() {
        StringBuffer sb = new StringBuffer();
        for (int i = 24; i > 0; i -= 12) {
            int n = Math.min(12, Math.abs(i));
            sb.append(Long.toString(Math.round(Math.random() * Math.pow(36, n)), 36));
        }
        return sb.toString();
    }

    /**
     * Finds an hkey that does not collide with another key. Most of the time, this will not have to recurse. And by most of the time, I literally mean
     * that out of 100000 iterations of the Hkey generation algorithm, there were ZERO collisions.
     */
    private String getHKey() {
        String key = this.generateHKey();
        // Sql query that finds all colliding hkeys.
        SqlQuery findColliding = OpenAuth.getInstance().getDatabase().createSqlQuery("select * from users where hkey = :key");
        findColliding.setParameter("key", key);
        List<SqlRow> collisions = findColliding.findList();
        if (collisions.size() == 0) {
            return key;
        }
        return this.getHKey();
    }

    /**
     * Allocates an hkey to a player.
     */
    public String allocateHKey(OAPlayer player) {
        DBPlayer playerData;
        synchronized(OpenAuth.databaseLock) {
            playerData = OpenAuth.getInstance().getDatabase().find(DBPlayer.class, player.getName());
        }
        if (playerData.getHkey() != null) {
            return playerData.getHkey();
        }
        String hkey = this.getHKey();
        playerData.setHkey(hkey);
        playerData.update();
        return hkey;
    }

    /**
     * Verify the connecting hostname of a player.
     */
    public boolean verifyHKey(OAPlayer player, String hostname) {
        DBPlayer playerData;
        synchronized(OpenAuth.databaseLock) {
            playerData = OpenAuth.getInstance().getDatabase().find(DBPlayer.class, player.getName());
        }
        if (playerData.getHkey() == null || this.hkAllowed == false) {
            return true; // HKA is disabled or the user has no HK -- allow them in.
        } else if ((playerData.getHkey() + this.hkBaseDomain).equals(hostname)) {
            return true;
        } else {
            return false;
        }
    }
}