package me.maiome.openauth.event;

import com.sk89q.util.StringUtil; // string processing

import java.util.logging.Logger; // java logging module

// bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

// internal
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;

public class OAListener implements Listener {

    private final OpenAuth controller;
    private final LogHandler log = new LogHandler();

    public OAListener(OpenAuth oa) {
        this.controller = oa;
    }

    /**
     * Called when a player tries to use a command.
     *
     * Some of this method is "borrowed" from WorldEdit.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;

        String[] split = event.getMessage().split(" ");

        if (split.length > 0) {
            split = this.controller.detectCommands(split);
            final String label = split[0];
            split[0] = "/" + split[0];
        }

        final String new_message = StringUtil.joinString(split, " ");
        if (!(new_message.equals(event.getMessage()))) {
            event.setMessage(new_message);
            this.controller.getServer().getPluginManager().callEvent(event);
            if (!(event.isCancelled())) {
                if (event.getMessage().length() > 0) {
                    this.controller.getServer().dispatchCommand(
                        event.getPlayer(),
                        event.getMessage().substring(1));
                }
                event.setCancelled(true);
            }
        }
    }

    /** 
     * Called when a player logs in.
     *
     * This will be used to for ban and whitelist features.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        OAPlayer player = this.controller.wrap(event);
        this.controller.getOAServer().getWhitelistHandler().processPlayerJoin(event, player);
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) return;
        this.controller.getOAServer().getLoginHandler().processPlayerLogin(event, player);
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) return;
        player.initSession();
        player.getSession().setLoginLocation();
        return;
    }

    /**
     * Called when a player joins the server (after the player logs in).
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        OAPlayer player = this.controller.wrap(event.getPlayer());
        if (ConfigInventory.MAIN.getConfig().getBoolean("auth.greet-players", true) == true) {
            if (!(player.getPlayer().hasPlayedBefore())) {
                player.sendMessage(ChatColor.GREEN + String.format(
                    "Welcome to %s, %s! To player on our server, we require you to register with OpenAuth.",
                    player.getServer().getServer().getServerName(), player.getName()
                ));
                player.sendMessage(ChatColor.GREEN + "To register, use this command: /oa register <password>");
            } else if (player.getPlayer().hasPlayedBefore() && !(player.getSession().isIdentified())) {
                player.sendMessage(ChatColor.GREEN + String.format(
                    "Welcome back to %s, %s! Please login to play! To login, use: /oa login <password>",
                    player.getServer().getServer().getServerName(), player.getName()
                ));
            } else if (player.getPlayer().hasPlayedBefore() && player.getSession().isIdentified()) {
                player.sendMessage(ChatColor.GREEN + String.format(
                    "Welcome back to %s, %s! Enjoy your stay!",
                    player.getServer().getServer().getServerName(), player.getName()
                ));
            }
        }
    }

    /**
     * Called when a player quits.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        OAPlayer player = this.controller.wrap(event.getPlayer());
        this.controller.getOAServer().getLoginHandler().processPlayerLogout(player);
        return;
    }

    /**
     * Called when a player chats.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(PlayerChatEvent event) {
        OAPlayer player = this.controller.wrap(event.getPlayer());

        try {
            if (player.getSession().isFrozen() == true &&
               ConfigInventory.MAIN.getConfig().getBoolean("auth.freeze-actions.chat", true) == true) {

                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You must identify first to chat.");
               return;
            }
        } catch (java.lang.Exception e) {
            log.warning(String.format("Caught Exception %s while processing onPlayerChat.", e.getMessage()));
            log.exDebug(e.toString());
        }
        return;
    }

    /**
     * Called when a player moves.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        OAPlayer player = this.controller.wrap(event.getPlayer());

        if (event.getFrom().getX() == event.getTo().getX() &&
            event.getFrom().getY() == event.getTo().getY() &&
            event.getFrom().getZ() == event.getTo().getZ()) {

            return;
        }

        player.moved();
        return;
    }

    /**
     * Called when a player tries to place blocks.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        OAPlayer player = this.controller.wrap(event.getPlayer());

        if (player.getSession().isFrozen() == true &&
            ConfigInventory.MAIN.getConfig().getBoolean("auth.freeze-actions.block-place", true) == true) {

            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You must identify first to build blocks.");
            return;
        }
        return;
    }


    /**
     * Called when a player tries to build blocks.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        OAPlayer player = this.controller.wrap(event.getPlayer());

        if (player.getSession().isFrozen() == true &&
            ConfigInventory.MAIN.getConfig().getBoolean("auth.freeze-actions.block-break", true) == true) {

            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You must identify first to break blocks.");
            return;
        }
        return;
    }

    /**
     * Called when a player interacts with another entity.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        OAPlayer player = this.controller.wrap(event.getPlayer());
        Entity targ_e = event.getRightClicked();
        if (player.getSession().playerUsingWand() &&
            player.getSession().hasAction() && player.getSession().getAction().allowed() &&
            targ_e instanceof Player) {

            player.getSession().runAction(this.controller.wrap((Player) targ_e));
        } else if (player.getSession().playerUsingWand() &&
            player.getSession().hasAction() &&
            player.getSession().getAction().allowsAnyEntityTarget() == true &&
            player.getSession().getAction().allowed() &&
            targ_e instanceof Entity) {

            player.getSession().runAction(targ_e);
        } else if (player.getSession().playerUsingWand() && player.getSession().hasAction() && !(player.getSession().getAction().allowed())) {
            player.sendMessage(ChatColor.RED + "You don't have the permission to use this action, sorry ;p");
            return;
        }
    }

    /**
     * Called when a player interacts with a block-type entity (blocks, levers, etc).
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        OAPlayer player = this.controller.wrap(event.getPlayer());
        Block targ_b = (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) ?
            event.getClickedBlock() : null;
        if (player.getSession().playerUsingWand() &&
            player.getSession().hasAction() && player.getSession().getAction().allowed() &&
            player.getSession().getAction().requiresEntityTarget() == false &&
            targ_b != null) {

            player.getSession().runAction(targ_b);
        } else if (player.getSession().playerUsingWand() && player.getSession().hasAction() && !(player.getSession().getAction().allowed())) {
            player.sendMessage(ChatColor.RED + "You don't have the permission to use this action, sorry ;p");
            return;
        }
    }
}
