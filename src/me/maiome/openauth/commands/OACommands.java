package me.maiome.openauth.commands;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.ConfigInventory;

// command framework imports
import com.sk89q.minecraft.util.commands.*;

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

// java imports
import java.util.Map;
import java.util.HashMap;

// etCommon imports
import net.eisental.common.page.Pager;

public class OACommands {

    private static OpenAuth controller;
    private static final LogHandler log = new LogHandler();

    public OACommands (OpenAuth openauth) {
        controller = openauth;
    }

    public static class OAParentCommand {

        private final OpenAuth controller;

        public OAParentCommand (OpenAuth openauth) {
            controller = openauth;
        }

        @Command(aliases = {"openauth", "oa"}, desc = "OpenAuth commands",
                 flags = "", min = 1)
        @NestedCommand({OACommands.class, OAActionCommands.class})
        public static void openAuth() {}
    }

    @Command(aliases = {"version"}, usage = "", desc = "OpenAuth version information", min = 0, max = 0)
    public static void version(CommandContext args, CommandSender sender) throws CommandException {
        sender.sendMessage(ChatColor.GREEN + controller.getDescription().getFullName());
    }

    @Command(aliases = {"login"}, usage = "<username> <password>", desc = "Login to the server.",
             min = 2, max = 2)
    public static void login(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = controller.wrapOAPlayer((Player) sender);
        String username = args.getString(0), password = args.getString(1);
        if (controller.getOAServer().getLoginHandler().processPlayerIdentification(username, password)) {
            player.sendMessage(ChatColor.GREEN + "You have been logged in as '" + username + "'.");
            return;
        } else {
            player.sendMessage(ChatColor.RED + "Invalid username/password.");
            return;
        }
    }

    @Console
    @Command(aliases = {"ban-ip"}, usage = "<user> [reason]", desc = "Allows banning of a user by IP.",
             min = 1, max = -1)
    @CommandPermissions({ "openauth.ban.ip" })
    public static void banIP(CommandContext args, CommandSender sender) throws CommandException {
        String reason;
        if (controller.wrapOAPlayer(args.getString(0)) == null) {
            sender.sendMessage(ChatColor.BLUE + "Please provide a valid player to ban.");
            return;
        }
        if (args.argsLength() > 1) {
            // there has been a reason given.
            reason = args.getJoinedStrings(1);
            controller.getOAServer().banPlayerByIP(controller.wrapOAPlayer(args.getString(0)), reason);
            controller.getOAServer().kickPlayer(controller.wrapOAPlayer(args.getString(0)), reason);
            sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been banned.", args.getString(0)));
        } else {
            // there has not been a reason given
            controller.getOAServer().banPlayerByIP(controller.wrapOAPlayer(args.getString(0)));
            controller.getOAServer().kickPlayer(controller.wrapOAPlayer(args.getString(0)));
            sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been banned.", args.getString(0)));
        }
    }

    @Console
    @Command(aliases = {"unban-ip"}, usage = "<user|IP>", desc = "Allows removal of an IP ban.",
             min = 1, max = 1)
    @CommandPermissions({ "openauth.unban.ip" })
    public static void unbanIP(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = controller.forciblyWrapOAPlayer(args.getString(0));
        if (player == null) {
            sender.sendMessage(ChatColor.BLUE + "You need to provide the banned IP, as this user does not exist in my memory.");
            return;
        }
        try {
            controller.getOAServer().unbanPlayerByIP(player.getIP());
            sender.sendMessage(ChatColor.BLUE + String.format("Player %s(%s) has been unbanned.", player.getName(), player.getIP()));
        } catch (java.lang.NullPointerException e) {
            log.warning("Was not able to get a valid IP from OAPlayer instance (" + player.toString() + "). Please report this.");
        }
        return;
    }

    @Console
    @Command(aliases = {"list-bans"}, usage = "[-n|-i]", desc = "Lists all bans in a list specified by -n or -i",
             flags = "ni", max = 1)
    @CommandPermissions({ "openauth.list.bans" })
    public static void listBans(CommandContext args, CommandSender sender) throws CommandException {
        if (args.hasFlag('n')) {
            // name bans
            Map<String, String> name_bans = controller.getOAServer().getNameBans();
            String list = new String();
            for (Map.Entry<String, String> entry : name_bans.entrySet()) {
                list += " - " + entry.getKey() + " : " + entry.getValue() + "\n";
            }
            if (list.length() == 0) {
                list = " - No bans exist.\n";
            }
            Pager.beginPaging(
                sender,
                "===[OpenAuth] Name ban list===",
                list,
                ChatColor.GREEN,
                ChatColor.RED
            );
        } else if (args.hasFlag('i')) {
            // ip bans
            Map<String, String> ip_bans = controller.getOAServer().getIPBans();
            String list = new String();
            for (Map.Entry<String, String> entry : ip_bans.entrySet()) {
                list += " - " + entry.getKey() + " : " + entry.getValue() + "\n";
            }
            if (list.length() == 0) {
                list = " - No bans exist.\n";
            }
            Pager.beginPaging(
                sender,
                "===[OpenAuth] IP ban list===",
                list,
                ChatColor.GREEN,
                ChatColor.RED
            );
        } else {
            // no flags
            sender.sendMessage("You must specify either -n or -i when you use this command.");
            return;
        }
    }
}