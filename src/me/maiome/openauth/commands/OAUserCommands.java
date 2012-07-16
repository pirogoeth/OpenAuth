package me.maiome.openauth.commands;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.events.*;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.ConfigInventory;

// command framework imports
import com.sk89q.minecraft.util.commands.*;

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// java imports
import java.util.Map;
import java.util.HashMap;

// etCommon imports
import net.eisental.common.page.Pager;

public class OAUserCommands {

    private static OpenAuth controller;
    private static final LogHandler log = new LogHandler();

    public static class UserParentCommand {

        private final OpenAuth controller;

        public UserParentCommand (OpenAuth openauth) {
            controller = openauth;
        }

        @Command(aliases = {"user", "u"}, desc = "OpenAuth user commands",
                 flags = "", min = 1)
        @NestedCommand({OAUserCommands.class})
        public static void userparent() {}
    }

    public OAUserCommands (OpenAuth openauth) {
        controller = openauth;
    }

    public static Class getParent() {
        return UserParentCommand.class;
    }

    @Console
    @Command(aliases = {"freeze"}, usage = "<user>", desc = "Freezes a player.",
             min = 1, max = 1)
    @CommandPermissions({ "openauth.admin.user.freeze" })
    public static void freeze(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = OAPlayer.getPlayer(args.getString(0));
        if (player == null) {
            sender.sendMessage(ChatColor.BLUE + args.getString(0) + " does not exist.");
            return;
        }
        if (player.getSession().isFrozen()) {
            sender.sendMessage(ChatColor.BLUE + player.getName() + " is already frozen!");
            return;
        }
        player.getSession().setFrozen(true);
        player.sendMessage(ChatColor.BLUE + "You have been frozen!");
        sender.sendMessage(ChatColor.BLUE + player.getName() + " has been frozen!");
    }

    @Console
    @Command(aliases = {"unfreeze"}, usage = "<user>", desc = "Unfreezes a player.",
             min = 1, max = 1)
    @CommandPermissions({ "openauth.admin.user.unfreeze" })
    public static void unfreeze(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = OAPlayer.getPlayer(args.getString(0));
        if (player == null) {
            sender.sendMessage(ChatColor.BLUE + args.getString(0) + " does not exist.");
            return;
        }
        if (!(player.getSession().isFrozen())) {
            sender.sendMessage(ChatColor.BLUE + player.getName() + " is not frozen!");
            return;
        }
        player.getSession().setFrozen(false);
        player.sendMessage(ChatColor.BLUE + "You have been unfrozen!");
        sender.sendMessage(ChatColor.BLUE + player.getName() + " has been unfrozen!");
    }

    @Console
    @Command(aliases = {"destroyaccount"}, usage = "<username>", desc = "Destroys an account owned by <user>.",
             min = 1, max = 1)
    @CommandPermissions({ "openauth.admin.user.destroyaccount" })
    public static void destroyact(CommandContext args, CommandSender sender) throws CommandException {
        String user = args.getString(0);
        String nullstring = null;
        if (!(OpenAuth.getOAServer().getLoginHandler().isRegistered(user))) {
            sender.sendMessage(ChatColor.BLUE + "Account " + user + " does not exist.");
            return;
        }
        controller.getOAServer().getLoginHandler().processPlayerRegistration(args.getString(0), nullstring);
        sender.sendMessage(ChatColor.BLUE + "[" + args.getString(0) + "] has been reset!");
    }
}