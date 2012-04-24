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
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;

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

    @Command(aliases = {"login"}, usage = "<password>", desc = "Login to the server.",
             min = 1, max = 1)
    public static void login(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = controller.wrap((Player) sender);
        String password = args.getString(0);
        if (controller.getOAServer().getLoginHandler().processPlayerIdentification(player, password)) {
            player.getSession().setIdentified(true, true);
            player.sendMessage(ChatColor.GREEN + "You have been logged in as '" + player.getName() + "'.");
            return;
        } else {
            player.sendMessage(ChatColor.RED + "Invalid username/password.");
            return;
        }
    }

    @Command(aliases = {"logout"}, usage = "", desc = "Logout from the plugin.",
             max = 0)
    public static void logout(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = controller.wrap((Player) sender);
        player.getSession().setIdentified(false, true);
        player.sendMessage(ChatColor.BLUE + "You have been logged out.");
        return;
    }

    @Command(aliases = {"changepass"}, usage = "<oldpass> <newpass>", desc = "Change your current password.",
             min = 2, max = 2)
    public static void changepass(CommandContext args, CommandContext sender) throws CommandException {
        OAPlayer player = controller.wrap((Player) sender);
        String oldpass = args.getString(0), newpass = args.getString(1);
        if (!(controller.getOAServer().getLoginHandler().isRegistered(player))) {
            player.sendMessage(ChatColor.RED + "How can you change your password if you aren't even registered? -_-'");
            return;
        } else if (!(player.getSession().isIdentified())) {
            player.sendMessage(ChatColor.RED + "You must be logged in to change your password, sorry.");
            return;
        }
        if (controller.getOAServer().getLoginHandler().compareToCurrent(player, oldpass)) {
            controller.getOAServer().getLoginHandler().processPlayerRegistration(player, newpass);
            player.sendMessage(ChatColor.BLUE + "Your password has been changed!");
            return;
        }
    }

    @Command(aliases = {"register"}, usage = "<password>", desc = "Login to the server.",
             min = 1, max = 1)
    public static void register(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = controller.wrap((Player) sender);
        String password = args.getString(0);
        if (!(controller.getOAServer().getLoginHandler().isRegistered(player))) {
            controller.getOAServer().getLoginHandler().processPlayerRegistration(player, password);
            player.getSession().setIdentified(true, true);
            player.sendMessage(ChatColor.BLUE + "You have been registered and logged in as '" + player.getName() + "'.");
            return;
        } else if (controller.getOAServer().getLoginHandler().isRegistered(player)) {
            player.sendMessage(ChatColor.RED + "This player account is already registered.");
            return;
        }
    }

    @Command(aliases = {"wand"}, usage = "", desc = "Gives the player a wand.",
             max = 0)
    @CommandPermissions({ "openauth.wand" })
    public static void wand(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = controller.wrap((Player) sender);
        player.getSession().giveWand();
    }

    @Console
    @Command(aliases = {"ban-ip"}, usage = "<user> [reason]", desc = "Allows banning of a user by IP.",
             min = 1, max = -1)
    @CommandPermissions({ "openauth.ban.ip" })
    public static void banIP(CommandContext args, CommandSender sender) throws CommandException {
        String reason;
        if (controller.wrap(args.getString(0)) == null) {
            sender.sendMessage(ChatColor.BLUE + "Please provide a valid player to ban.");
            return;
        }
        if (args.argsLength() > 1) {
            // there has been a reason given.
            reason = args.getJoinedStrings(1);
            controller.getOAServer().banPlayerByIP(controller.wrap(args.getString(0)), reason);
            controller.getOAServer().kickPlayer(controller.wrap(args.getString(0)), reason);
            sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been banned.", args.getString(0)));
        } else {
            // there has not been a reason given
            controller.getOAServer().banPlayerByIP(controller.wrap(args.getString(0)));
            controller.getOAServer().kickPlayer(controller.wrap(args.getString(0)));
            sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been banned.", args.getString(0)));
        }
    }

    @Console
    @Command(aliases = {"unban-ip"}, usage = "<user|IP>", desc = "Allows removal of an IP ban.",
             min = 1, max = 1)
    @CommandPermissions({ "openauth.unban.ip" })
    public static void unbanIP(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = controller.forciblyWrapOAPlayer(args.getString(0));
        if (player == null && args.getString(0).charAt(0) != '/') {
            sender.sendMessage(ChatColor.BLUE + "You need to provide the banned IP, as this user does not exist in my memory.");
            return;
        } else if (args.getString(0).charAt(0) == '/') {
            controller.getOAServer().unbanPlayerByIP(args.getString(0));
            sender.sendMessage(ChatColor.BLUE + String.format("IP %s has been removed from the ban list.", args.getString(0)));
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
    @Command(aliases = {"ban-name"}, usage = "<user> [reason]", desc = "Allows banning of a user by name.",
             min = 1, max = -1)
    @CommandPermissions({ "openauth.ban.name" })
    public static void banName(CommandContext args, CommandSender sender) throws CommandException {
        if (controller.wrappable(args.getString(0))) {
            OAPlayer player = controller.wrap(args.getString(0));
            if (args.argsLength() > 1) {
                controller.getOAServer().kickPlayer(player, args.getJoinedStrings(1));
                controller.getOAServer().banPlayerByName(player, args.getJoinedStrings(1));
            } else {
                controller.getOAServer().kickPlayer(player);
                controller.getOAServer().banPlayerByName(player);
            }
            sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been banned.", args.getString(0)));
            return;
        } else if (!(controller.wrappable(args.getString(0)))) {
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
        if (!(controller.getOAServer().hasNameBan(args.getString(0)))) {
            sender.sendMessage(ChatColor.BLUE + String.format("There is not an existing ban for %s.", args.getString(0)));
            return;
        } else {
            controller.getOAServer().unbanPlayerByName(args.getString(0));
            sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been unbanned.", args.getString(0)));
        }
        return;
    }

    @Console
    @Command(aliases = {"whitelist-add"}, usage = "<user>", desc = "Allows whitelisting of players.",
             min = 1, max = 1)
    @CommandPermissions({ "openauth.whitelist.add" })
    public static void whitelistadd(CommandContext args, CommandSender sender) throws CommandException {
        if (controller.getOAServer().getWhitelistHandler().isEnabled()) {
            controller.getOAServer().getWhitelistHandler().whitelistPlayer(args.getString(0));
            sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been whitelisted.", args.getString(0)));
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "Whitelisting is not enabled.");
        return;
    }

    @Console
    @Command(aliases = {"whitelist-show"}, usage = "", desc = "Prints the whitelist.",
             max = 0)
    @CommandPermissions({ "openauth.whitelist.show" })
    public static void whitelistshow(CommandContext args, CommandSender sender) throws CommandException {
        if (controller.getOAServer().getWhitelistHandler().isEnabled()) {
            sender.sendMessage(ChatColor.BLUE + "Whitelist:");
            for (String name : controller.getOAServer().getWhitelistHandler().getWhitelist()) {
                sender.sendMessage(ChatColor.BLUE + String.format(" => %s", name));
            }
        } else {
            sender.sendMessage(ChatColor.GREEN + "Whitelisting is disabled!");
            return;
        }
        return;
    }

    @Console
    @Command(aliases = {"whitelist-remove"}, usage = "<user>", desc = "Allows dewhitelisting of players.",
             min = 1, max = 1)
    @CommandPermissions({ "openauth.whitelist.remove" })
    public static void whitelistremove(CommandContext args, CommandSender sender) throws CommandException {
        if (controller.getOAServer().getWhitelistHandler().isEnabled()) {
            controller.getOAServer().getWhitelistHandler().unwhitelistPlayer(args.getString(0));
            sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been unwhitelisted.", args.getString(0)));
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "Whitelisting is not enabled.");
        return;
    }

    @Command(aliases = {"test-placement"}, desc = "", max = 0)
    public static void ptest(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = controller.wrap((Player) sender);
        Location loc = player.getLocation();
        Direction d = player.getSimpleDirection();
        if (d == Direction.NORTH) {
            // player is x-aligned, decreasing x value
            loc.getWorld().getBlockAt(new Location(loc.getWorld(), loc.getBlockX() + 5, loc.getBlockY(), loc.getBlockZ() + 3)).setTypeId(1);
            loc.getWorld().getBlockAt(new Location(loc.getWorld(), loc.getBlockX() + 5, loc.getBlockY(), loc.getBlockZ() - 3)).setTypeId(1);
        } else if (d == Direction.WEST) {
            // player is z-aligned, incresing z value
            loc.getWorld().getBlockAt(new Location(loc.getWorld(), loc.getBlockX() + 3, loc.getBlockY(), loc.getBlockZ() - 5)).setTypeId(1);
            loc.getWorld().getBlockAt(new Location(loc.getWorld(), loc.getBlockX() - 3, loc.getBlockY(), loc.getBlockZ() - 5)).setTypeId(1);
        } else if (d == Direction.EAST) {
            // player is z-aligned, decreasing z value
            loc.getWorld().getBlockAt(new Location(loc.getWorld(), loc.getBlockX() + 3, loc.getBlockY(), loc.getBlockZ() + 5)).setTypeId(1);
            loc.getWorld().getBlockAt(new Location(loc.getWorld(), loc.getBlockX() - 3, loc.getBlockY(), loc.getBlockZ() + 5)).setTypeId(1);
        } else if (d == Direction.SOUTH) {
            // player is x-aligned, increasing x value
            loc.getWorld().getBlockAt(new Location(loc.getWorld(), loc.getBlockX() - 5, loc.getBlockY(), loc.getBlockZ() + 3)).setTypeId(1);
            loc.getWorld().getBlockAt(new Location(loc.getWorld(), loc.getBlockX() - 5, loc.getBlockY(), loc.getBlockZ() - 3)).setTypeId(1);
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
                list += " - " + entry.getKey() + " : " + ((MemorySection) entry.getValue()).getString(entry.getKey()) + "\n";
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