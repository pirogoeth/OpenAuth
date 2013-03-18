package me.maiome.openauth.database;

import java.util.*;

import com.avaje.ebean.*;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.util.LogHandler;

public class DatabaseUpdater {

    private static LogHandler log = new LogHandler();

    public static String[] sqlStatements = {
        "ALTER TABLE users ADD COLUMN hkey TEXT;", // 0: Adds a new column to the DBPlayer table that allows for host key storage.
    };

    public static void runUpdates() {
        for (int i = 0; i < sqlStatements.length; i++) {
            SqlUpdate update = OpenAuth.getInstance().getDatabase().createSqlUpdate(sqlStatements[i]);
            try {
                update.execute();
            } catch (java.lang.Exception e) {
                log.warning(String.format(" - Error while running query %d: %s", i, e.getMessage()));
                continue;
            }
            log.debug(String.format(" - Database query #%d succeeded!", i));
        }
    }
}