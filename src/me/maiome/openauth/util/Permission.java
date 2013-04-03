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

    private static HandlerType handler = HandlerType.NONE;
    private static Plugin permissions;

    public static void search() {
        LogHandler.info("Searching for a suitable permissions plugin.");
        Plugin permissions = null;
        // check for a permissions method
        if (packageExists("ru.tehkode.permissions.bukkit.PermissionsEx")) {
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
                LogHandler.info("Using [" + permissions.getDescription().getFullName() + "] for Permissions.");
                break;
            case PERMISSIONS_EX:
                LogHandler.info("Using [PermissionsEx " + permissions.getDescription().getVersion() + "] for Permissions.");
                break;
            case SUPERPERMS:
                LogHandler.info("Using Bukkit SuperPerms for Permissions.");
                LogHandler.info("SuperPerms provider: [" + permissions.getDescription().getFullName() + "].");
                break;
            case OP:
                LogHandler.info("Using OP system for Permissions.");
                break;
        }
    }

    private static boolean isPluginEnabled(String pluginname) {
        return OpenAuth.getInstance().getServer().getPluginManager().isPluginEnabled(pluginname);
    }

    private static Plugin getPlugin(String pluginname) {
        return (Plugin) OpenAuth.getInstance().getServer().getPluginManager().getPlugin(pluginname);
    }

    public static boolean packageExists(String pkg) {
        try {
            Class.forName(pkg);
            return true;
        } catch (java.lang.Exception e) {
            return false;
        }
    }

    private enum HandlerType {
        NONE,
        PERMISSIONS_EX,
        PERMISSIONS,
        OP,
        SUPERPERMS
    }

    public static boolean has(Player player, String node) {
        switch (handler) {
            case PERMISSIONS:
                return ((Permissions) permissions).getHandler().has(player, node);
            case PERMISSIONS_EX:
                return PermissionsEx.getPermissionManager().has(player, node);
            case SUPERPERMS:
                return player.hasPermission(node);
            case OP:
                return player.isOp();
        }
        return true;
    }

    public static boolean has(Player player, String node, boolean def) {
        switch (handler) {
            case PERMISSIONS:
                return ((Permissions) permissions).getHandler().has(player, node);
            case PERMISSIONS_EX:
                return PermissionsEx.getPermissionManager().has(player, node);
            case SUPERPERMS:
                return player.hasPermission(node);
            case OP:
                return def ? true : player.isOp();
        }
        return def;
    }
}
