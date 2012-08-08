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
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

// java imports
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

// etCommon imports
import net.eisental.common.page.Pager;

public class OACommands {

    private static OpenAuth controller;
    private static final LogHandler log = new LogHandler();

    public OACommands (OpenAuth openauth) {
        controller = openauth;
    }

    public static class OAParentCommand {

        private static OpenAuth controller;

        public OAParentCommand (OpenAuth openauth) {
            controller = openauth;
        }

        @Command(aliases = {"openauth", "oa"}, desc = "OpenAuth commands",
                 flags = "", min = 1)
        @NestedCommand({ OACommands.class, OAActionCommands.ActionParentCommand.class, OABanCommands.BanParentCommand.class,
                         OAUserCommands.UserParentCommand.class, OAWhitelistCommands.WhitelistParentCommand.class })
        public static void openAuth() {}
    }

    @Command(aliases = {"version"}, usage = "", desc = "OpenAuth version information", min = 0, max = 0)
    public static void version(CommandContext args, CommandSender sender) throws CommandException {
        String hashtag = (YamlConfiguration.loadConfiguration(controller.getResource("plugin.yml"))).getString("hashtag");
        sender.sendMessage(ChatColor.GREEN + String.format(
            "%s-%s", controller.getDescription().getFullName(), hashtag));
    }

    @Command(aliases = {"login"}, usage = "<password>", desc = "Login to the server.",
             min = 1, max = 1)
    public static void login(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = OAPlayer.getPlayer((Player) sender);
        String password = args.getString(0);
        if (controller.getOAServer().getLoginHandler().processPlayerIdentification(player, password)) {
            if (player.getSession().setIdentified(true, true)) {
                player.sendMessage(ChatColor.GREEN + "You have been logged in as '" + player.getName() + "'.");
                // do my crappy location fixing algorithm
                player.fixLocation();
                return;
            }
            return;
        } else {
            player.sendMessage(ChatColor.RED + "Invalid username/password.");
            return;
        }
    }

    @Command(aliases = {"logout"}, usage = "", desc = "Logout from the server.",
             max = 0)
    public static void logout(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = OAPlayer.getPlayer((Player) sender);
        if (player.getSession().setIdentified(false, true)) {
            player.sendMessage(ChatColor.BLUE + "You have been logged out.");
        }
        return;
    }

    @Command(aliases = {"changepass"}, usage = "<oldpass> <newpass>", desc = "Change your current password.",
             min = 2, max = 2)
    public static void changepass(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = OAPlayer.getPlayer((Player) sender);
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
        } else {
            player.sendMessage(ChatColor.BLUE + "Your current password was invalid, try again.");
            return;
        }
    }

    @Command(aliases = {"register"}, usage = "<password>", desc = "Login to the server.",
             min = 1, max = 1)
    public static void register(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = OAPlayer.getPlayer((Player) sender);
        String password = args.getString(0);
        if (!(controller.getOAServer().getLoginHandler().isRegistered(player))) {
            controller.getOAServer().getLoginHandler().processPlayerRegistration(player, password);
            player.getSession().setIdentified(true, true);
            // notification is taken care of in processPlayerRegistration now
            // player.sendMessage(ChatColor.BLUE + "You have been registered and logged in as '" + player.getName() + "'.");
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
        OAPlayer player = OAPlayer.getPlayer((Player) sender);
        player.getSession().giveWand();
    }

    @Command(aliases = {"save"}, usage = "", desc = "Saves the data configuration.", max = 0)
    @CommandPermissions({ "openauth.admin.save" })
    public static void savedata(CommandContext args, CommandSender sender) throws CommandException {
        OpenAuth.getOAServer().saveBans(false);
        ConfigInventory.DATA.save();
        OpenAuth.getOAServer().getWhitelistHandler().saveWhitelist();
        sender.sendMessage(ChatColor.GREEN + "OpenAuth data has been saved.");
    }

    @Command(aliases = {"test"}, desc = "This is always here to test some new thing.", max = 1)
    public static void ptest(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = OAPlayer.getPlayer((Player) sender);
        int dist = ((args.argsLength() > 0) ? Integer.parseInt(args.getString(0)) : 5);
        HashSet<Byte> transparent = new HashSet<Byte>();
        transparent.add(new Integer(0).byteValue());
        transparent.add(new Integer(1).byteValue());
        List<Block> sight = player.getPlayer().getLineOfSight(transparent, dist);
        StringBuilder blocklist = new StringBuilder();
        for (Block b : sight) {
            blocklist.append(b.getTypeId() + ",");
        }
        sender.sendMessage(ChatColor.BLUE + blocklist.toString());
    }
}