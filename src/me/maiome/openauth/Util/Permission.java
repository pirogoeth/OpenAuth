package me.maiome.openauth.Util;

// java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;
// bukkit imports
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;
// permissions imports
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
// internal imports
import me.maiome.openauth.OpenAuth;

public class Permission {

    private OpenAuth plugin;
    // public static Permissions permissions;
    private static HandlerType handler = HandlerType.OP;
    private static PermissionHandler permissionPlugin;

    public Permission (OpenAuth instance) {
        plugin = instance;
        Logger log = Logger.getLogger("Minecraft");
        Plugin permissions = plugin.getServer().getPluginManager().getPlugin("Permissions");
        Plugin superperms_s = plugin.getServer().getPluginManager().getPlugin("PermissionsBukkit");
        if (permissions != null) {
            permissionPlugin = ((Permissions)permissions).getHandler();
            handler = HandlerType.PERMISSIONS;
            log.info("[OpenAuth] Permissions plugin detected. Using " + permissions.getDescription().getFullName());
        } else if (permissions == null && superperms_s != null) {
            handler = HandlerType.SUPERPERMS;
            log.info("[OpenAuth] Using Bukkit SuperPerms, PermissionsBukkit detected.");
        } else {
            log.info("[OpenAuth] No Permissions plugin detected. Using OP");
        }
    }

    private enum HandlerType {
        PERMISSIONS,
        OP,
        SUPERPERMS
    }

    public static boolean has (Player p, String node) {
        switch (handler) {
            case PERMISSIONS:
                return permissionPlugin.has(p, node);
            case OP:
                return p.isOp();
            case SUPERPERMS:
                return p.hasPermission(node);
        }
        return true;
    }

    private static boolean hasPermission (Player p, String node, boolean def) {
        switch (handler) {
            case PERMISSIONS:
                return permissionPlugin.has(p, node);
            case OP:
                return def ? true : p.isOp();
            case SUPERPERMS:
                return p.hasPermission(node);
        }
        return def;
    }
}
