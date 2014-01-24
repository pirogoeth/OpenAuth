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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

// internal
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.bukkit.events.*;
import me.maiome.openauth.security.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.*;

public class OAListener extends Reloadable implements Listener {

    private final OpenAuth controller;
    private final LogHandler log = new LogHandler();

    // onPlayerJoin fields
    private boolean joinAuth;
    private boolean joinGreet;
    private boolean joinHideInv;

    // onPlayerKick fields
    private boolean kickMoveTooQuick;

    // onPlayerChat fields
    private boolean chatFreeze;

    // onPlayerDroppedItem fields
    private boolean dropFreeze;

    // onPlayerTeleport fields
    private boolean teleportFreeze;

    // onBlockPlace fields
    private boolean blockPlaceFreeze;

    // onBlockBreak fields
    private boolean blockBreakFreeze;

    // onPlayerInteractEntity fields
    private boolean interactEntityFreeze;

    // onPlayerInteract fields
    private boolean interactFreeze;

    public OAListener() {
        this.reload();
        this.controller = OpenAuth.getInstance();
        this.setReloadable(this);
    }

    protected void reload() {
        this.joinAuth = Config.getConfig().getBoolean("auth.required", true);
        this.joinGreet = Config.getConfig().getBoolean("auth.greet-players", true);
        this.joinHideInv = Config.getConfig().getBoolean("auth.hide-inventory", false);
        this.kickMoveTooQuick = Config.getConfig().getBoolean("misc.catch-moving-too-quickly-kick", false);
        this.chatFreeze = Config.getConfig().getBoolean("auth.freeze-actions.chat", true);
        this.dropFreeze = Config.getConfig().getBoolean("auth.freeze-actions.drop", true);
        this.teleportFreeze = Config.getConfig().getBoolean("auth.freeze-actions.teleport", true);
        this.blockPlaceFreeze = Config.getConfig().getBoolean("auth.freeze-actions.block-place", true);
        this.blockBreakFreeze = Config.getConfig().getBoolean("auth.freeze-actions.block-break", true);
        this.interactEntityFreeze = Config.getConfig().getBoolean("auth.freeze-actions.entity-interact", true);
        this.interactFreeze = Config.getConfig().getBoolean("auth.freeze-actions.block-interact", true);
    }

    /**
     * Called when a player tries to use a command.
     *
     * Some of this method is "borrowed" from WorldEdit.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;

        OAPlayer player = OAPlayer.getPlayer((Player) event.getPlayer());

        String[] split = event.getMessage().split(" ");

        if (split.length > 0) {
            split = this.controller.detectCommands(split);
            final String label = split[0];
            split[0] = "/" + split[0];
        }

        if (player.getSession().isFrozen() == true && player.getSession().isIdentified() == true) {
            if (((!(split[0].equals("/oa")) && !(split[0].equals("/openauth"))) && (!(split[1].equals("login")) && !(split[1].equals("register")))) && !(split[0].equals("/worldedit"))) {
                player.sendMessage(ChatColor.GREEN + "You are frozen.");
                event.setCancelled(true);
                return;
            }
        }

        if (player.getSession().isFrozen() == true &&
            Config.getConfig().getBoolean("auth.freeze-actions.commands", true) == true) {
            try {
                if (((!(split[0].equals("/oa")) && !(split[0].equals("/login")) && !(split[0].equals("/register")) && !(split[0].equals("/openauth"))) && (!(split[1].equals("login")) && !(split[1].equals("register")))) && !(split[0].equals("/worldedit"))) {
                    player.sendMessage(ChatColor.GREEN + "You must identify to use commands.");
                    event.setCancelled(true);
                    return;
                }
            } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                if (!(split[0].equals("/oa")) && !(split[0].equals("/openauth")) && !(split[0].equals("/worldedit")) && !(split[0].equals("/login")) && !(split[0].equals("/register"))) {
                    player.sendMessage(ChatColor.GREEN + "You must identify to use commands.");
                    event.setCancelled(true);
                    return;
                }
            }
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
     * Catches player kicks.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerKick(PlayerKickEvent event) {
        if (this.kickMoveTooQuick) {
            if (event.getReason().startsWith("You moved too quickly")) {
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
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) return; // derp, totally forgot. causes problems with sk89q's hostkeys.
        OAPlayer player = OAPlayer.getPlayer(event);

        try {
            Session ts = SessionController.getInstance().get(player);
            if (ts == null) {
                throw new Exception("Session was null.");
            }
        } catch (Exception e) {
            SessionController.getInstance().create(player);
        }

        LockdownManager lck = LockdownManager.getInstance();

        if (lck.isLocked() && !(player.hasPermission("openauth.lockdown.exempt"))) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, lck.getLockReason());
            return;
        } else if (lck.isLocked() && player.hasPermission("openauth.lockdown.exempt")) {
            player.sendMessage(ChatColor.RED + "Warning! Server lockdown is still active! Reason: " + lck.getLockReason());
            return;
        }

        if (HKAManager.getInstance().verifyHKey(player, event.getHostname()) == false) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Your host key is incorrect.");
            return;
        }


        OAServer.getInstance().getWhitelistHandler().processPlayerJoin(event, player);
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) return;
        OAServer.getInstance().getLoginHandler().processPlayerLogin(event, player);
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) return;
        player.getSession().setLoginLocation();
        return;
    }

    /**
     * Called when a player joins the server (after the player logs in).
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());
        player.update();
        String color = String.format("%s%s", ChatColor.BOLD, ChatColor.LIGHT_PURPLE);
        if (this.joinHideInv && !(player.getSession().isIdentified())) {
            player.getSession().hideInventory();
        }
        if (this.joinGreet) {
            if (!(this.joinAuth)) {
                player.sendMessage(color + String.format(
                    "Welcome to %s, %s! We hope you have a wonderful time!",
                    player.getServer().getServer().getServerName(), player.getName()
                ));
            } else if (!(player.getPlayer().hasPlayedBefore()) || !(OAServer.getInstance().getLoginHandler().isRegistered(player))) {
                player.sendMessage(color + String.format(
                    "Welcome to %s, %s! To play on our server, we require you to register.",
                    player.getServer().getServer().getServerName(), player.getName()
                ));
                player.sendMessage(color + "To register, use this command: /register <password>");
            } else if (player.getPlayer().hasPlayedBefore() && OAServer.getInstance().getLoginHandler().isRegistered(player) && !(player.getSession().isIdentified())) {
                player.sendMessage(color + String.format(
                    "Welcome back to %s, %s! Please login to play! To login, use: /login <password>",
                    player.getServer().getServer().getServerName(), player.getName()
                ));
            } else if (player.getSession().isIdentified()) {
                player.sendMessage(color + String.format(
                    "Welcome back to %s, %s! Enjoy your stay!",
                    player.getServer().getServer().getServerName(), player.getName()
                ));
            } else {
                player.sendMessage(color + String.format(
                    "Hi, %s. You must be some strange transient entity from another dimension (or at least another server)...",
                    player.getName()
                ));
            }
            player.sendMessage(color + String.format(
                "You are currently in %s, at %s,%s,%s. The weather in %s is looking quite %s today.",
                player.getWorld().getName(), player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ(), player.getWorld().getName(), ((player.getWorld().hasStorm() == false) ? "sunny" : "stormy")
            ));
        }
    }

    /**
     * Called when a player quits.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());
        OAServer.getInstance().getLoginHandler().processPlayerLogout(player);
        return;
    }

    /**
     * Called when a player chats.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerChat(PlayerChatEvent event) {
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());

        if (player.getSession().isFrozen() == true && player.getSession().isIdentified() == true) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You are frozen.");
            return;
        }

        try {
            if (player.getSession().isIdentified() == false && this.chatFreeze) {

                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You must identify first to chat.");
                return;
            }
        } catch (java.lang.Exception e) {
            log.warning(String.format("Caught Exception %s while processing onPlayerChat.", e.getMessage()));
            log.debug(e.toString());
        }
        return;
    }

    /**
     * Called when a player drops an item.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDroppedItem(PlayerDropItemEvent event) {
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());

        if (player.getSession().isIdentified() == false && this.dropFreeze) {

            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You must first identify to drop items.");
            return;
        }
    }

    /**
     * Called when a player moves.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());

        if (event.getFrom().getX() == event.getTo().getX() &&
            event.getFrom().getY() == event.getTo().getY() &&
            event.getFrom().getZ() == event.getTo().getZ()) {

            return;
        }

        player.moved(event);
        return;
    }

    /**
     * Called when a player tries to teleport.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());

        if (player.getSession().isFrozen() == true && player.getSession().isIdentified() == true) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You are frozen.");
            return;
        }

        if (player.getSession().isIdentified() == false && this.teleportFreeze) {

            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You must identify first to teleport.");
            return;
        }
        return;
    }


    /**
     * Called when a player tries to place blocks.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());

        if (player.getSession().isFrozen() == true && player.getSession().isIdentified() == true) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You are frozen.");
            return;
        }

        if (player.getSession().isIdentified() == false && this.blockPlaceFreeze) {

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
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());

        if (player.getSession().isFrozen() == true && player.getSession().isIdentified() == true) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You are frozen.");
            return;
        }

        if (player.getSession().isIdentified() == false && this.blockBreakFreeze) {

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
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());
        Entity targetEntity = event.getRightClicked();
        if (player.getSession().isFrozen() == true && player.getSession().isIdentified() == true) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You are frozen.");
            return;
        }

        if (player.getSession().isIdentified() == false && this.interactEntityFreeze) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You must first identify to interact with entities.");
        }

        if (player.getSession().playerUsingWand() &&
                player.getSession().hasAction() && player.getSession().getAction().allowed() &&
                targetEntity instanceof Player) {

            player.getSession().runAction(OAPlayer.getPlayer((Player) targetEntity));
        } else if (player.getSession().playerUsingWand() &&
                player.getSession().hasAction() &&
                player.getSession().getAction().allowsAnyEntityTarget() == true &&
                player.getSession().getAction().allowed() &&
                targetEntity instanceof Entity) {

            player.getSession().runAction(targetEntity);
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
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());
        Block targetBlock = ((event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) ? event.getClickedBlock() : null);

        if (player.getSession().isFrozen() == true && player.getSession().isIdentified() == true) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You are frozen.");
            return;
        }

        if (player.getSession().isIdentified() == false && this.interactFreeze) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You must first identify to interact with blocks.");
        }

        if (player.getSession().playerUsingWand() &&
                player.getSession().hasAction() && player.getSession().getAction().allowed() &&
                player.getSession().getAction().requiresEntityTarget() == false &&
                targetBlock != null) {

            player.getSession().runAction(targetBlock);
        } else if (player.getSession().playerUsingWand() && player.getSession().hasAction() && !(player.getSession().getAction().allowed())) {
            player.sendMessage(ChatColor.RED + "You don't have the permission to use this action, sorry ;p");
            return;
        }
    }
}
