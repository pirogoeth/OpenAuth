import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.mixins.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.LogHandler;

import com.sk89q.minecraft.util.commands.*;

import java.lang.reflect.*;
import java.util.*;

public class Notifications implements IMixin, Listener {

    private final String name = "Notifications";
    private OpenAuth controller;
    private static final LogHandler log = new LogHandler();

    public Notifications() {
        this.controller = (OpenAuth) OpenAuth.getInstance();
    }

    public Notifications(OpenAuth controller) {
        this.controller = controller;
    }

    public String getName() {
        return this.name;
    }

    public void onInit() {
        this.controller.getCommandsManagerRegistration().register(Notifications.class);
        this.controller.getServer().getPluginManager().registerEvents(this, OpenAuth.getInstance());
    }

    public void onTeardown() { }

    /**
     * Note:
     *
     * On PlayerChangedWorldEvent, getFrom() [of type World] is provided.
     * This event is passed AFTER the player changes world, so you can get the
     * player's new world from player.getLocation().getWorld().
     */
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        // send a message to all players in the previous world.
        for (Player p : event.getFrom().getPlayers()) {
           p.sendMessage(ChatColor.GREEN + "[" + ChatColor.BLUE + player.getName() + ChatColor.GREEN + "] has left the world.");
        }
        // send a message to all players in the new world.
        for (Player p : player.getLocation().getWorld().getPlayers()) {
           p.sendMessage(ChatColor.GREEN + "[" + ChatColor.BLUE + player.getName() + ChatColor.GREEN + "] has entered the world.");
        }
    }
}