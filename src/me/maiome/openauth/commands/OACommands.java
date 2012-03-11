package me.maiome.openauth.commands;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.ConfigInventory;

// command framework imports
import com.sk89q.minecraft.util.commands.*;

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class OACommands {

    private static OpenAuth controller;

    public OACommands (OpenAuth openauth) {
        controller = openauth;
    }

    public static class OAParentCommand {

        private final OpenAuth controller;

        public OAParentCommand (OpenAuth openauth) {
            controller = openauth;
        }

        @Command(aliases = {"openauth", "oa"}, desc = "OpenAuth commands",
                 flags = "d", min = 1)
        @NestedCommand({OACommands.class})
        public static void openAuth() {}
    }

    @Command(aliases = {"version"}, usage = "", desc = "OpenAuth version information", min = 0, max = 0)
    public static void version(CommandContext args, CommandSender sender) throws CommandException {
        sender.sendMessage(ChatColor.GREEN + controller.getDescription().getFullName());
    }

    @Console
    @Command(aliases = {"ban-ip"}, usage = "<user> [reason]", desc = "Allows banning of a user by IP.",
             min = 1, max = -1)
    @CommandPermissions({ "openauth.ban" })
    public static void ban(CommandContext args, CommandSender sender) throws CommandException {
        String reason;
        if (controller.wrapOAPlayer(args.getString(0)) == null) {
            sender.sendMessage(ChatColor.BLUE + "Please provide a valid player to ban.");
            return;
        }
        if (args.argsLength() > 1) {
            // there has been a reason given.
            reason = args.getJoinedStrings(1);
            controller.getOAServer().banPlayerByIP(controller.wrapOAPlayer(args.getString(0)), reason);
            sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been banned.", args.getString(0)));
        } else {
            // there has not been a reason given
            controller.getOAServer().banPlayerByIP(controller.wrapOAPlayer(args.getString(0)));
            sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been banned.", args.getString(0)));
        }
    }
}