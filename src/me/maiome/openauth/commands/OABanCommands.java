package me.maiome.openauth.commands;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAPlayer.Direction;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.ConfigInventory;

// command framework imports
import com.sk89q.minecraft.util.commands.*;

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;

// java imports
import java.util.Map;
import java.util.HashMap;

// etCommon imports
import net.eisental.common.page.Pager;

public class OABanCommands {

    private static OpenAuth controller;
    private static final LogHandler log = new LogHandler();

    public static class BanParentCommand {

        private final OpenAuth controller;

        public BanParentCommand (OpenAuth openauth) {
            controller = openauth;
        }

        @Command(aliases = {"bans", "b"}, desc = "OpenAuth ban commands",
                 flags = "", min = 1)
        @NestedCommand({OABanCommands.class})
        public static void oabans() {}
    }

    public OABanCommands (OpenAuth openauth) {
        controller = openauth;
    }

    public static Class getParent() {
        return BanParentCommand.class;
    }

    @Console
    @Command(aliases = {"ban-ip"}, usage = "<user> [reason]", desc = "Allows banning of a user by IP.",
             min = 1, max = -1)
    @CommandPermissions({ "openauth.ban.ip" })
    public static void banIP(CommandContext args, CommandSender sender) throws CommandException {
        String reason = (args.argsLength() > 1 ? args.getJoinedStrings(1) : "No reason given.");
        try {
            if (OAPlayer.getPlayer(args.getString(0)) == null) {
                sender.sendMessage(ChatColor.BLUE + "Please provide a valid player to ban.");
                return;
            }
            controller.getOAServer().banPlayerByIP(OAPlayer.getPlayer(args.getString(0)), reason);
            controller.getOAServer().kickPlayer(OAPlayer.getPlayer(args.getString(0)), reason);
            sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been banned.", args.getString(0)));
        } catch (java.lang.NullPointerException e) {
            controller.getOAServer().banPlayerByIP(args.getString(0), reason);
            sender.sendMessage(ChatColor.BLUE + String.format("IP %s has been banned.", args.getString(0)));
        }
    }

    @Console
    @Command(aliases = {"unban-ip"}, usage = "<user|/IP>", desc = "Allows removal of an IP ban.",
             min = 1)
    @CommandPermissions({ "openauth.unban.ip" })
    public static void unbanIP(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = OAPlayer.getPlayer(args.getString(0));
        String reason = (args.argsLength() > 1 ? args.getJoinedStrings(1) : "No reason given.");
        if (player == null && args.getString(0).charAt(0) != '/') {
            sender.sendMessage(ChatColor.BLUE + "You need to provide the banned IP, as this user does not exist in my memory. (Prefix the IP with /)");
            return;
        } else if (args.getString(0).charAt(0) == '/') {
            controller.getOAServer().unbanPlayerByIP(args.getString(0));
            sender.sendMessage(ChatColor.BLUE + String.format("IP %s has been removed from the ban list.", args.getString(0)));
            log.info(String.format("%s was unbanned! [Reason: %s]", args.getString(0), args.getJoinedStrings(1)));
            return;
        }
        try {
            controller.getOAServer().unbanPlayerByIP(player.getIP());
            sender.sendMessage(ChatColor.BLUE + String.format("Player %s(%s) has been unbanned.", player.getName(), player.getIP()));
            log.info(String.format("%s(%s) was unbanned! [Reason: %s]", player.getName(), player.getIP(), args.getJoinedStrings(1)));
        } catch (java.lang.NullPointerException e) {
            log.warning("Was not able to get a valid IP from OAPlayer instance (" + player.toString() + "). Please report this.");
        }
        return;
    }

    @Console
    @Command(aliases = {"ban-name"}, usage = "<user> [reason]", desc = "Allows banning of a user by name.",
             min = 1, max = -1)
    @CommandPermissions({ "openauth.ban.name" })
    public static void banName(CommandContext args, CommandSender sender) throws CommandException {
        if (OAPlayer.hasPlayer(args.getString(0))) {
            OAPlayer player = OAPlayer.getPlayer(args.getString(0));
            if (args.argsLength() > 1) {
                controller.getOAServer().kickPlayer(player, args.getJoinedStrings(1));
                controller.getOAServer().banPlayerByName(player, args.getJoinedStrings(1));
            } else {
                controller.getOAServer().kickPlayer(player);
                controller.getOAServer().banPlayerByName(player);
            }
            sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been banned.", args.getString(0)));
            return;
        } else if (!(OAPlayer.hasPlayer(args.getString(0)))) {
            // banning a player and that is all. no kicking, just preventing a join.
            if (args.argsLength() > 1) {
                controller.getOAServer().banPlayerByName(args.getString(0), args.getJoinedStrings(1));
                sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been banned. [%s]", args.getString(0), args.getJoinedStrings(1)));
                return;
            } else {
                controller.getOAServer().banPlayerByName(args.getString(0));
                sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been banned.", args.getString(0)));
                return;
            }
        }
    }

    @Console
    @Command(aliases = {"unban-name"}, usage = "<user|IP>", desc = "Allows removal of an name ban.",
             min = 1, max = 1)
    @CommandPermissions({ "openauth.unban.name" })
    public static void unbanName(CommandContext args, CommandSender sender) throws CommandException {
        String reason = (args.argsLength() > 1 ? args.getJoinedStrings(1) : "No reason given.");
        if (!(controller.getOAServer().hasNameBan(args.getString(0)))) {
            sender.sendMessage(ChatColor.BLUE + String.format("There is not an existing ban for %s.", args.getString(0)));
            return;
        } else {
            controller.getOAServer().unbanPlayerByName(args.getString(0));
            sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been unbanned.", args.getString(0)));
            log.info(String.format("%s has been unbanned! [Reason: %s]", args.getString(0), reason));
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
            Map<String, Object> name_bans = controller.getOAServer().getNameBans();
            String list = new String();
            for (Map.Entry<String, Object> entry : name_bans.entrySet()) {
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
            Map<String, Object> ip_bans = controller.getOAServer().getIPBans();
            String list = new String();
            for (Map.Entry<String, Object> entry : ip_bans.entrySet()) {
                list += " - " + entry.getKey().replace(',', '.') + " : " + entry.getValue() + "\n";
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