package me.maiome.openauth.commands;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAPlayer.Direction;
import me.maiome.openauth.database.DBBanRecord;
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
import java.util.*;

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
    @Command(aliases = {"ban"}, usage = "<user> [reason]", desc = "Allows banning of a user.",
             min = 1, max = -1, flags = "ni")
    @CommandPermissions({ "openauth.ban" })
    public static void ban(CommandContext args, CommandSender sender) throws CommandException {
        int type = 0;
        String reason = (args.argsLength() > 1 ? args.getJoinedStrings(1) : "No reason given.");
        if (args.hasFlag('n')) {
            type = 1;
        } else if (args.hasFlag('i')) {
            type = 2;
        } else {
            sender.sendMessage("Please provide -n (name ban) or -i (IP ban) to use this command.");
            return;
        }
        if (OAPlayer.getPlayer(args.getString(0)) == null) {
            sender.sendMessage(ChatColor.BLUE + "Please provide a valid player to ban.");
            return;
        }
        controller.getOAServer().banPlayer(OAPlayer.getPlayer(args.getString(0)), type, sender.getName(), reason);
        controller.getOAServer().kickPlayer(OAPlayer.getPlayer(args.getString(0)), reason);
        sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been banned.", args.getString(0)));
    }

    @Console
    @Command(aliases = {"mb", "manualban"}, usage = "<username> <name if -n, IP if -i> <reason>", desc = "Allows manual adding of bans.",
             min = 3, max = -1, flags = "ni")
    @CommandPermissions({ "openauth.ban.manual" })
    public static void manualban(CommandContext args, CommandSender sender) throws CommandException {
        int type = 0;
        String reason = (args.argsLength() > 1 ? args.getJoinedStrings(1) : "No reason given.");
        if (args.hasFlag('n')) {
            type = 1;
        } else if (args.hasFlag('i')) {
            type = 2;
        } else {
            sender.sendMessage("Please provide -n (name ban) or -i (IP ban) to use this command.");
            return;
        }
        DBBanRecord record = new DBBanRecord(args.getString(0), type, sender.getName(), reason, (type == 2 ? args.getString(0) : ""));
        sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been banned.", args.getString(0)));
    }

    @Console
    @Command(aliases = {"mu", "manualunban"}, usage = "<username>", desc = "Allows manual removal of bans.",
             min = 1)
    @CommandPermissions({ "openauth.unban.manual" })
    public static void manualunban(CommandContext args, CommandSender sender) throws CommandException {
        DBBanRecord record = OpenAuth.getInstance().getDatabase().find(DBBanRecord.class, args.getString(0));
        if (record == null) {
            sender.sendMessage(ChatColor.BLUE + "There is no ban record matching that player.");
            return;
        }
        record.delete();
        sender.sendMessage(ChatColor.BLUE + "Player " + args.getString(0) + " has been unbanned.");
    };

    @Console
    @Command(aliases = {"unban"}, usage = "<username>", desc = "Allows removal of a ban.",
             min = 1, max = 1)
    @CommandPermissions({ "openauth.unban" })
    public static void unbanIP(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = OAPlayer.getPlayer(args.getString(0));
        if (controller.getOAServer().unbanPlayer(args.getString(0)) == true) {
            sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been unbanned.", player.getName(), player.getIP()));
            try {
                log.info(String.format("%s(%s) was unbanned! [Reason: %s]", player.getName(), player.getIP(), args.getJoinedStrings(1)));
            } catch (java.lang.IndexOutOfBoundsException e) {
                log.info(String.format("%s(%s) was unbanned!", player.getName(), player.getIP()));
            }
        } else {
            sender.sendMessage("Could not unban player " + args.getString(0));
        }
        return;
    }

    @Console
    @Command(aliases = {"list"}, usage = "[-n|-i]", desc = "Lists all bans in a list specified by -n or -i",
             flags = "ni", max = 1)
    @CommandPermissions({ "openauth.bans.list" })
    public static void listBans(CommandContext args, CommandSender sender) throws CommandException {
        if (args.hasFlag('n')) {
            // name bans
            List<DBBanRecord> bans = OpenAuth.getInstance().getDatabase().find(DBBanRecord.class).where().eq("type", 1).findList();
            String list = new String();
            for (DBBanRecord record : bans) {
                list += String.format(" - \u00A7a%s\u00A7f -=- banned by \u00A7a%s\u00A7f on \u00A7a%s\u00A7f for \u00A7a%s\u00A7f -=- criteria (T%d): \u00A7a%s\u00A7f\n", record.getName(), record.getBanner(), record.getTime().toString(), record.getReason(), record.getType(), record.getBannable());
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
            List<DBBanRecord> bans = OpenAuth.getInstance().getDatabase().find(DBBanRecord.class).where().eq("type", 2).findList();
            String list = new String();
            for (DBBanRecord record : bans) {
                list += String.format(" - \u00A7a%s\u00A7f -=- banned by \u00A7a%s\u00A7f on \u00A7a%s\u00A7f for \u00A7a%s\u00A7f -=- criteria (T%d): \u00A7a%s\u00A7f\n", record.getName(), record.getBanner(), record.getTime().toString(), record.getReason(), record.getType(), record.getBannable());
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