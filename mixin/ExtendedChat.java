import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.mixins.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;

import com.sk89q.minecraft.util.commands.*;

import java.lang.reflect.*;
import java.util.*;

public class ExtendedChat implements IMixin, Listener {

    private final String name = "ExtendedChat";
    private OpenAuth controller;
    private static final LogHandler log = new LogHandler();
    private static final String permissible = "openauth.extchat.staff";
    private final List<Player> staffChatList = new ArrayList<Player>();

    public ExtendedChat() {
        this((OpenAuth) OpenAuth.getInstance());
    }

    public ExtendedChat(OpenAuth instance) {
        this.controller = instance;
    }

    public String getName() {
        return this.name;
    }

    public void onInit() {
        this.controller.getCommandsManagerRegistration().register(ExtendedChat.class);
        this.controller.getServer().getPluginManager().registerEvents(this, OpenAuth.getInstance());
    }

    public void onTeardown() { }

    @Console
    @Command(aliases = {"staff", "s"}, usage = "[-i <username>]", desc = "Toggles between staff chat and regular chat.",
             max = 1, min = 0, flags = "i")
    @CommandPermissions({ permissible })
    public void staff(CommandContext args, CommandSender sender) {
        if (args.hasFlag('i')) {
            // this is an invite
            String username;
            try {
                username = args.getString(0);
            } catch (java.lang.Exception e) {
                sender.sendMessage("You must provide the name of a user to invite to the staff chat.");
                return;
            }
            OAPlayer invitee = OAPlayer.getPlayer(username);
            if (invitee == null) {
                sender.sendMessage("Player " + username + " is not online.");
                return;
            }
            if (invitee.hasPermission(permissible)) {
                invitee.sendMessage(ChatColor.GREEN + "You have been invited by " + sender.getName() + " to join the staff channel. Use /staff to switch.");
                sender.sendMessage(ChatColor.GREEN + "Player " + username + " has been invited to join the staff channel.");
                return;
            } else {
                sender.sendMessage("Player " + username + " does not have permission to join the staff channel.");
                return;
            }
        }
        if (sender.getName().equals("CONSOLE")) {
            sender.sendMessage("You're the console...you can already see the staff channel.");
            return;
        }
        Player player = (Player) sender;
        if (this.staffChatList.contains(player)) {
            this.staffChatList.remove(player);
            sender.sendMessage(ChatColor.BLUE + "You have switched to normal game chat.");
            return;
        } else if (!(this.staffChatList.contains(player))) {
            this.staffChatList.add(player);
            sender.sendMessage(ChatColor.BLUE + "You have switched to the staff chat.");
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());
        if (event.getMessage().charAt(0) == '@') {
                // this is a PM.
                event.setCancelled(true);
                String _target = event.getMessage().split(" ")[0].substring(1);
                OAPlayer target = OAPlayer.getPlayer(OpenAuth.getInstance().getServer().getPlayer(_target));
                if (target == null) {
                    player.sendMessage("Player " + _target + " is not online.");
                    return;
            }
            player.sendMessage("[PM to " + target.getName() + "]: " + event.getMessage().substring(event.getMessage().indexOf(" ")));
            target.sendMessage("[PM from " + player.getName() + "]: " + event.getMessage().substring(event.getMessage().indexOf(" ")));
            return;
        } else if (this.staffChatList.contains(event.getPlayer())) {
            // this is a staff chat channel message.
            event.setCancelled(true);
            List<Player> players = Arrays.asList(OpenAuth.getInstance().getServer().getOnlinePlayers());
            String format = "[\u00A7c%p\u00A7f] <%s> %m";
            format.replace("%p", "Staff");
            format.replace("%s", player.getName());
            format.replace("%m", event.getMessage());
            player.sendMessage(format);
            for (Player target : players) {
                if (target.isOnline() && !(target.getName().equals(player.getName()))) {
                    target.sendMessage(format);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (this.staffChatList.contains(event.getPlayer().getName())) {
            event.getPlayer().sendMessage(ChatColor.BLUE + "NOTICE: Your chat focus is still on staff chat!");
            return;
        }
    }
}