package me.maiome.openauth.commands;

// internal imports
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.bukkit.events.*;
import me.maiome.openauth.util.*;

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

public class OAPageCommands {

    private static OpenAuth controller;
    private static final LogHandler log = new LogHandler();

    public static class PageParentCommand {

        private final OpenAuth controller;

        public PageParentCommand (OpenAuth openauth) {
            controller = openauth;
        }

        @Command(aliases = {"page", "pg"}, desc = "OpenAuth paging commands",
                 flags = "", min = 1)
        @NestedCommand({OAPageCommands.class})
        public static void pageparent() {}
    }

    public OAPageCommands (OpenAuth openauth) {
        controller = openauth;
    }

    public static Class getParent() {
        return PageParentCommand.class;
    }

    @Console
    @Command(aliases = {"next"}, usage = "", desc = "Display the next page.",
             max = 0)
    public static void nextPage(CommandContext args, CommandSender sender) throws CommandException {
        Pager.nextPage(sender);
    }

    @Console
    @Command(aliases = {"previous"}, usage = "", desc = "Displays the previous page.",
             max = 0)
    public static void previousPage(CommandContext args, CommandSender sender) throws CommandException {
        Pager.previousPage(sender);
    }

    @Console
    @Command(aliases = {"goto"}, usage = "<page no.>", desc = "Goes to the specified page.",
             min = 1, max = 1)
    public static void gotoPage(CommandContext args, CommandSender sender) throws CommandException {
        Pager.gotoPage(sender, Integer.parseInt(args.getString(0)));
    }

    @Console
    @Command(aliases = {"last"}, usage = "", desc = "Goes to the last page.",
             max = 0)
    public static void lastPage(CommandContext args, CommandSender sender) throws CommandException {
        Pager.lastPage(sender);
    }
}
