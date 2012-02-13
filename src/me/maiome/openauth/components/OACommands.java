package me.maiome.openauth.components;

// internal imports
import me.maiome.openauth.OpenAuth;
import me.maiome.openauth.LogHandler;
import me.maiome.openauth.ConfigInventory;

// component imports
import com.zachsthings.libcomponents.AbstractComponent;
import com.zachsthings.libcomponents.ComponentInformation;

// command framework imports
import com.sk89q.minecraft.util.commands.*;

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class OACommands {
    
    public static class OAParentCommand {
        @Command(aliases = {"openauth", "oauth", "oa"},
                 desc = "OpenAuth commands", flags = "d",
                 min = 1, max = 3)
        @NestedCommand( {OACommands.class} )
        public static void openAuth() {
        }
    }
    
    @Command(aliases = {"version"},
             usage = "", desc = "OpenAuth version information",
             min = 0, max = 0)
    public static void version(CommandContext args, CommandSender sender) throws CommandException {
        sender.sendMessage(ChatColor.BLUE + "OpenAuth " + OpenAuth.inst().getDescription().getVersion());
        sender.sendMessage(ChatColor.BLUE + "http://maio.me");
    }
    
    @Command(aliases = {"reload"},
             usage = "", desc = "Reload OpenAuth's settings",
             min = 0, max = 0)
    @CommandPermissions( {"openauth.reload"} )
    public static void reload(CommandContext args, CommandSender sender) throws CommandException {
        OpenAuth.inst().populateConfiguration();
        OpenAuth.inst().getComponentManager().reloadComponents();
        
        sender.sendMessage(ChatColor.BLUE + "OpenAuth's configuration has been reloaded.");
    }
}