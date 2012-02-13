package me.maiome.openauth.util;

// java imports
import java.util.HashMap;
import java.util.HashSet;

// bukkit imports
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

// permissions imports
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.util.LogHandler;

public class Permission {

    private static OpenAuth plugin;
    private static HandlerType handler = HandlerType.NONE;
    private static Plugin permissions;

    public Permission (OpenAuth instance) {
        plugin = instance;
        LogHandler log = new LogHandler();
        log.info("Searching for a suitable permissions plugin.");
        Plugin permissions = null;
        // check for a permissions method
        if (isPluginEnabled("PermissionsEx")) {
            handler = HandlerType.PERMISSIONS_EX;
            permissions = getPlugin("PermissionsEx");
        } else if (isPluginEnabled("PermissionsBukkit")) {
            handler = HandlerType.SUPERPERMS;
            permissions = getPlugin("PermissionsBukkit");
        } else if (isPluginEnabled("Permissions")) {
            handler = HandlerType.PERMISSIONS;
            permissions = getPlugin("Permissions");
        } else {
            handler = HandlerType.OP;
        }
        // notify to what we are using
        switch (handler) {
            case PERMISSIONS:
                log.info("Using [" + permissions.getDescription().getFullName() + "] for Permissions.");
            case PERMISSIONS_EX:
                log.info("Using [PermissionsEx " + permissions.getDescription().getVersion() + "] for Permissions.");
            case SUPERPERMS:
                log.info("Using Bukkit SuperPerms for Permissions.");
                log.info("SuperPerms provider: [" + permissions.getDescription().getFullName() + "].");
            case OP:
                log.info("Using OP system for Permissions.");
        }
    }

    private static boolean isPluginEnabled (String pluginname) {
        return plugin.getServer().getPluginManager().isPluginEnabled(pluginname);
    }

    private static Plugin getPlugin (String pluginname) {
        return (Plugin) plugin.getServer().getPluginManager().getPlugin(pluginname);
    }

    private enum HandlerType {
        NONE,
        PERMISSIONS_EX,
        PERMISSIONS,
        OP,
        SUPERPERMS
    }

    public static boolean has (Player p, String node) {
        switch (handler) {
            case PERMISSIONS:
                return ((Permissions) permissions).getHandler().has(p, node);
            case PERMISSIONS_EX:
                return PermissionsEx.getPermissionManager().has(p, node);
            case SUPERPERMS:
                return p.hasPermission(node);
            case OP:
                return p.isOp();
        }
        return true;
    }

    private static boolean hasPermission (Player p, String node, boolean def) {
        switch (handler) {
            case PERMISSIONS:
                return ((Permissions) permissions).getHandler().has(p, node);
            case PERMISSIONS_EX:
                return PermissionsEx.getPermissionManager().has(p, node);
            case SUPERPERMS:
                return p.hasPermission(node);
            case OP:
                return def ? true : p.isOp();
        }
        return def;
    }
}
