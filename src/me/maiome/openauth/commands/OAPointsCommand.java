package me.maiome.openauth.commands;

// internal imports
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.util.*;

// java imports
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// bundled imports
import com.sk89q.minecraft.util.commands.*;

// etCommon imports
import net.eisental.common.page.Pager;

// bukkit
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.Location;

public class OAPointsCommand {

    private static OpenAuth controller;
    private static LogHandler log = new LogHandler();

    public OAPointsCommand (OpenAuth instance) {
        controller = instance;
    }

    public static class OAPointsParentCommand {

        private final OpenAuth controller;

        public OAPointsParentCommand(OpenAuth instance) {
            controller = instance;
        }

        @Command(aliases = {"oap"}, desc = "Point management commands",
                 min = 1, max = 3)
        @CommandPermissions({ "openauth.location.points" })
        @NestedCommand({ OAPointsCommand.class })
        public static void points(CommandContext args, CommandSender sender) throws CommandException {}
    }

    @Command(aliases = {"add"}, desc = "Add a point", max = 1, min = 1)
    @CommandPermissions({ "openauth.location.points.add" })
    public static void add(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = controller.wrap((Player) sender);
        String name = args.getString(0);
        if (!(player.hasSavedLocation(name))) {
            player.saveLocation(name);
            player.sendMessage(ChatColor.GREEN + String.format("Saved point '%s'.", name));
        } else {
            player.sendMessage(ChatColor.RED + "That point already exists!");
        }
        return;
    }

    @Command(aliases = {"del"}, desc = "Delete a point", max = 1, min = 1)
    @CommandPermissions({ "openauth.location.points.delete" })
    public static void delete(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = controller.wrap((Player) sender);
        String name = args.getString(0);
        if (!(player.hasSavedLocation(name))) {
            player.sendMessage(ChatColor.RED + "That point doesn't exist!");
        } else {
            player.deleteLocation(name);
            player.sendMessage(ChatColor.BLUE + String.format("Point '%s' has been deleted.", name));
        }
        return;
    }

    @Command(aliases = {"tp", "teleport"}, desc = "Teleport to a point", max = 1, min = 1)
    @CommandPermissions({ "openauth.location.points.teleport" })
    public static void teleport(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = controller.wrap((Player) sender);
        String name = args.getString(0);
        if (!(player.hasSavedLocation(name))) {
            player.sendMessage(ChatColor.RED + String.format("Point %s doesn't exist!", name));
        } else {
            player.setLocation(player.getSavedLocation(name));
            player.sendMessage(ChatColor.BLUE + String.format("You have been teleported to point %s!", name));
        }
        return;
    }

    @Command(aliases = {"list"}, desc = "List your points", max = 0)
    @CommandPermissions({ "openauth.location.points.list" })
    public static void list(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = controller.wrap((Player) sender);
        if (player.getSavedLocations().size() == 0) {
            player.sendMessage(ChatColor.RED + "You have no points to display.");
            return;
        } else {
            String list_str = "";
            for (Map.Entry<String, Object> entry : player.getSavedLocations().entrySet()) {
                if (ConfigInventory.MAIN.getConfig().getBoolean("points.list-world-only") &&
                  !(((Location) entry.getValue()).getWorld().getName().equals(player.getLocation().getWorld().getName()))) {
                    continue;
                } else {
                    list_str += String.format(" - %s\n", entry.getKey());
                }
            }
            Pager.beginPaging(sender, "===Point List===",
              list_str, ChatColor.GREEN, ChatColor.RED);
            return;
        }
    }
}