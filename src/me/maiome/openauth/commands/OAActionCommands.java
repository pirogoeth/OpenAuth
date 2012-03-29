package me.maiome.openauth.commands;

// internal imports
import me.maiome.openauth.actions.*;
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
import org.bukkit.entity.Player;

// java imports
import java.util.Map;
import java.util.HashMap;

// etCommon imports
import net.eisental.common.page.Pager;

public class OAActionCommands {

    private static OpenAuth controller;
    private static final LogHandler log = new LogHandler();

    public OAActionCommands (OpenAuth openauth) {
        controller = openauth;
    }

    @Command(aliases = {"set-action"}, usage = "<action name>", min = 1, max = 1,
             flags = "c", desc = "Sets the action performed by the OAWand.")
    @CommandPermissions({"openauth.wand.set-action"})
    public static void setaction(CommandContext args, CommandSender sender) {
        OAPlayer player = controller.wrapOAPlayer((Player) sender);
        if (args.hasFlag('c')) {
            player.getSession().clearAction();
            player.sendMessage(ChatColor.BLUE + "Cleared wand action.");
            return;
        }
        if (Actions.actionExists(args.getString(0).toLowerCase())) {
            try {
                player.getSession().clearAction();
                player.getSession().setAction(args.getString(0).toLowerCase());
            } catch (java.lang.NullPointerException e) {
                player.initSession();
                player.sendMessage(ChatColor.RED + "An error occurred while setting your action.");
            } // the player has no session.
            player.sendMessage(ChatColor.BLUE + String.format("Action %s has been activated.", args.getString(0).toLowerCase()));
            return;
        } else {
            player.sendMessage(ChatColor.RED + "That action does not exist.");
            return;
        }
    }

    @Command(aliases = {"clear-action"}, usage = "", max = 0,
             desc = "Clears the user's current action.")
    @CommandPermissions({"openauth.wand.clear-action"})
    public static void clearaction(CommandContext args, CommandSender sender) {
        OAPlayer player = controller.wrapOAPlayer((Player) sender);

        player.getSession().clearAction();
        player.sendMessage(ChatColor.BLUE + "Cleared wand action.");
        return;
    }

    @Command(aliases = {"undo-action"}, usage = "", min = 0, max = 2,
             flags = "i", desc = "Undo the last action on the list.")
    @CommandPermissions({"openauth.wand.undo-action"})
    public static void undoaction(CommandContext args, CommandSender sender) {
        OAPlayer player = controller.wrapOAPlayer((Player) sender);
        if (args.hasFlag('i')) {
            // this means the player is going to undo the last 'i' actions.
            player.getSession().undoLastActions(new Integer(args.getString(0)));
            player.sendMessage(ChatColor.BLUE + String.format("Your last %s actions have been undone.", args.getString(0)));
            return;
        }

        player.getSession().undoLastAction();
        player.sendMessage(ChatColor.BLUE + "Action has been undone.");
    }
}