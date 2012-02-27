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

    private static OpenAuth oa;

    public OACommands (OpenAuth openauth) {
        oa = openauth;
    }

    public static class OAParentCommand {

        private final OpenAuth oa;

        public OAParentCommand (OpenAuth openauth) {
            oa = openauth;
        }

        @Command(aliases = {"openauth", "oauth", "oa"}, desc = "OpenAuth commands",
                 flags = "d", min = 1, max = 3)
        @NestedCommand({OACommands.class})
        public static void openAuth() {}
    }

    @Command(aliases = {"version"}, usage = "", desc = "OpenAuth version information", min = 0, max = 0)
    public static void version(CommandContext args, CommandSender sender) throws CommandException {
        sender.sendMessage(ChatColor.GREEN + oa.getDescription().getFullName());
    }

}