package me.maiome.openauth.commands;

// internal imports
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.security.HKAManager;
import me.maiome.openauth.util.LogHandler;

// command framework imports
import com.sk89q.minecraft.util.commands.*;

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OAHKAuthManagementCommands {

    private static OpenAuth controller;
    private static final LogHandler log = new LogHandler();

    public OAHKAuthManagementCommands (OpenAuth openauth) {
        controller = openauth;
    }

    public static class OAHKAManagementParentCommand {

        private static OpenAuth controller;

        public OAHKAManagementParentCommand (OpenAuth openauth) {
            controller = openauth;
        }

        @Command(aliases = {"hka"}, desc = "Host Key Auth management commands.",
                 flags = "", min = 1)
        @NestedCommand({OAHKAuthManagementCommands.class})
        public static void hka() {}
    }

    @Command(aliases = {"activate"}, desc = "Activate a host key for yourself.",
             max = 0)
    public static void activateHKA(CommandContext args, CommandSender sender) throws CommandException {
        if (sender instanceof org.bukkit.command.ConsoleCommandSender) {
            sender.sendMessage("You must activate your host key as a player, you may not do it on the console.");
            return;
        }
        OAPlayer player = OAPlayer.getPlayer((Player) sender);
        String newKey = HKAManager.getInstance().allocateHKey(player);
        String connectUrl = newKey + HKAManager.getInstance().getHKABaseDomain();
        player.sendMessage(ChatColor.GREEN + "Your host key has been set. Depending on server configuration, HKA may or may not be currently activated. It is recommended that you connect to your HKA url from now on just to prevent inability to access this server.");
        player.sendMessage(ChatColor.GREEN + "Your host key is: " + ChatColor.RED + connectUrl);
    }

    @Command(aliases = {"deactivate"}, desc = "Deactivate your host key.",
             max = 1, flags = "f")
    public static void deactivateHKA(CommandContext args, CommandSender sender) throws CommandException {
        if ((sender instanceof org.bukkit.command.ConsoleCommandSender) || (args.hasFlag('f') && sender.hasPermission("openauth.admin.drop-user-hkey"))) {
            String playerName;
            try {
                playerName = args.getString(0);
            } catch (java.lang.Exception e) {
                sender.sendMessage(ChatColor.RED + "You must specify a player to deactivate HKA for.");
                return;
            }
            OAPlayer player = OAPlayer.getPlayer(playerName);
            HKAManager.getInstance().deallocateHKey(player);
            sender.sendMessage(ChatColor.BLUE + "Host key for " + playerName + " has been deactivated.");
            return;
        } else {
            HKAManager.getInstance().deallocateHKey(OAPlayer.getPlayer(sender));
            sender.sendMessage(ChatColor.BLUE + "Your host key has been dectivated.");
        }
    }

}
