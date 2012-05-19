package me.maiome.openauth.commands;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.events.*;
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

public class OAWhitelistCommands {

    private static OpenAuth controller;
    private static final LogHandler log = new LogHandler();

    public OAWhitelistCommands (OpenAuth openauth) {
        controller = openauth;
    }

    public static class WhitelistParentCommand {

        private final OpenAuth controller;

        public WhitelistParentCommand (OpenAuth openauth) {
            controller = openauth;
        }

        @Command(aliases = {"whitelist", "w"}, desc = "OpenAuth whitelist commands",
                 flags = "", min = 1)
        @NestedCommand({OAWhitelistCommands.class})
        public static void openAuth() {}
    }

    @Console
    @Command(aliases = {"add"}, usage = "<user>", desc = "Allows whitelisting of players.",
             min = 1, max = 1)
    @CommandPermissions({ "openauth.whitelist.add" })
    public static void whitelistadd(CommandContext args, CommandSender sender) throws CommandException {
        if (controller.getOAServer().getWhitelistHandler().isEnabled()) {
            OAPlayerWhitelistedEvent event = new OAPlayerWhitelistedEvent(args.getString(0));
            OpenAuth.getOAServer().callEvent(event);
            if (event.isCancelled()) {
                sender.sendMessage(ChatColor.BLUE + String.format("Player %s is not able to be whitelisted.", args.getString(0)));
                return;
            }
            controller.getOAServer().getWhitelistHandler().whitelistPlayer(args.getString(0));
            sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been whitelisted.", args.getString(0)));
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "Whitelisting is not enabled.");
        return;
    }

    @Console
    @Command(aliases = {"show"}, usage = "", desc = "Prints the whitelist.",
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
    @Command(aliases = {"remove"}, usage = "<user>", desc = "Allows dewhitelisting of players.",
             min = 1, max = 1)
    @CommandPermissions({ "openauth.whitelist.remove" })
    public static void whitelistremove(CommandContext args, CommandSender sender) throws CommandException {
        if (controller.getOAServer().getWhitelistHandler().isEnabled()) {
            OAPlayerUnwhitelistedEvent event = new OAPlayerUnwhitelistedEvent(args.getString(0));
            OpenAuth.getOAServer().callEvent(event);
            if (event.isCancelled()) {
                sender.sendMessage(ChatColor.BLUE + String.format("Player %s is not able to be unwhitelisted.", args.getString(0)));
                return;
            }
            controller.getOAServer().getWhitelistHandler().unwhitelistPlayer(args.getString(0));
            sender.sendMessage(ChatColor.BLUE + String.format("Player %s has been unwhitelisted.", args.getString(0)));
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "Whitelisting is not enabled.");
        return;
    }
}