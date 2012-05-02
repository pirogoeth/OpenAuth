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

    public static class ActionParentCommand {

        private final OpenAuth controller;

        public ActionParentCommand(OpenAuth controller) {
            this.controller = controller;
        }

        @Command(aliases = {"action", "actions", "a"}, desc = "OpenAuth action commands",
                 flags = "")
        @NestedCommand({ OAActionCommands.class })
        public static void actions() {}
    }

    public OAActionCommands (OpenAuth openauth) {
        controller = openauth;
    }

    @Command(aliases = {"set"}, usage = "<action name>", min = 1,
             desc = "Sets the action performed by the OAWand.")
    @CommandPermissions({"openauth.wand.set-action"})
    public static void setaction(CommandContext args, CommandSender sender) {
        OAPlayer player = controller.wrap((Player) sender);
        if (Actions.actionExists(args.getString(0).toLowerCase())) {
            try {
                player.getSession().clearAction();
                player.getSession().setAction(args.getString(0).toLowerCase());
            } catch (java.lang.NullPointerException e) {
                player.initSession();
                player.sendMessage(ChatColor.RED + "An error occurred while setting your action.");
            } // the player has no session.
            if (args.argsLength() > 1) { // we have additional arguments.
                if (player.getSession().getAction().requiresArgs() || player.getSession().getAction().allowsArgs()) { // the action allows args
                    player.getSession().getAction().setArgs((args.getJoinedStrings(1)).split(" "));
                }
            } else if (args.argsLength() == 1 && player.getSession().getAction().requiresArgs()) {
                player.sendMessage(ChatColor.BLUE + "This action requires arguments. Please set them with /oa set-args.");
                return;
            }
            player.sendMessage(ChatColor.BLUE + String.format("Action %s has been activated.", args.getString(0).toLowerCase()));
            return;
        } else {
            player.sendMessage(ChatColor.RED + "That action does not exist.");
            return;
        }
    }

    @Command(aliases = {"set-args"}, usage = "<args>", min = 1,
             desc = "Sets arguments for the current action.")
    public static void setargs(CommandContext args, CommandSender sender) {
        OAPlayer player = controller.wrap((Player) sender);
        if (player.getSession().hasAction() &&
            (player.getSession().getAction().allowsArgs() || player.getSession().getAction().requiresArgs())) {

            try {
                player.getSession().getAction().setArgs((args.getJoinedStrings(0)).split(" "));
            } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                player.sendMessage(ChatColor.RED + "You have not provided enough args to set.");
                return;
            }
            player.sendMessage(ChatColor.BLUE + String.format("Args have been set for action %s.", player.getSession().getAction().getName()));
            return;
        }
    }

    @Command(aliases = {"clear"}, usage = "", max = 0,
             desc = "Clears the user's current action.")
    @CommandPermissions({"openauth.wand.clear-action"})
    public static void clearaction(CommandContext args, CommandSender sender) {
        OAPlayer player = controller.wrap((Player) sender);

        player.getSession().clearAction();
        player.sendMessage(ChatColor.BLUE + "Cleared wand action.");
        return;
    }

    @Command(aliases = {"undo"}, usage = "", min = 0, max = 2,
             flags = "i", desc = "Undo the last action on the list.")
    @CommandPermissions({"openauth.wand.undo-action"})
    public static void undoaction(CommandContext args, CommandSender sender) {
        OAPlayer player = controller.wrap((Player) sender);
        if (args.hasFlag('i')) {
            // this means the player is going to undo the last 'i' actions.
            player.getSession().undoLastActions(new Integer(args.getString(0)));
            return;
        }

        player.getSession().undoLastAction();
        player.sendMessage(ChatColor.BLUE + "Action has been undone.");
    }

    @Command(aliases = {"list"}, usage = "", max = 0,
             desc = "Provide a list of actions.")
    public static void listaction(CommandContext args, CommandSender sender) {
        String list = new String();
        for (Actions a : Actions.values()) {
            if (ConfigInventory.MAIN.getConfig().getBoolean("actions.verbose-list", false) == true) {
                list += String.format(" - %s (%s)\n", a.toString().toLowerCase(), a.getAction().getCanonicalName());
            } else {
                list += String.format(" - %s\n", a.toString().toLowerCase());
            }
        }
        Pager.beginPaging(
            sender,
            "==Action List==",
            list,
            ChatColor.GREEN,
            ChatColor.BLUE);
    }
}