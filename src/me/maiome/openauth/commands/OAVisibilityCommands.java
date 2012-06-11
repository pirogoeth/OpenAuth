package me.maiome.openauth.commands;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAPlayer.Direction;
import me.maiome.openauth.session.Session;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.ConfigInventory;

// command framework imports
import com.sk89q.minecraft.util.commands.*;

// minecraft server imports
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MobEffect;
import net.minecraft.server.MobEffectList;
import net.minecraft.server.Packet41MobEffect;
import net.minecraft.server.Packet42RemoveMobEffect;
import net.minecraft.server.Packet201PlayerInfo;

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;

// java imports
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

// etCommon imports
import net.eisental.common.page.Pager;

public class OAVisibilityCommands {

    private static OpenAuth controller;
    private static final LogHandler log = new LogHandler();

    public OAVisibilityCommands (OpenAuth openauth) {
        controller = openauth;
    }

    public static class OAVisibilityParent {

        private final OpenAuth controller;

        public OAVisibilityParent (OpenAuth openauth) {
            controller = openauth;
        }

        @Command(aliases = {"visibility", "vis"}, desc = "OpenAuth visibility commands",
                 flags = "", min = 1)
        @NestedCommand({ OAVisibilityCommands.class })
        public static void oavisibility() {}
    }

    @Command(aliases = {"toggle"}, flags = "s", desc = "Toggles player visibility.", max = 1)
    @CommandPermissions({ "openauth.admin.visibility.toggle" })
    public static void togglevis(CommandContext args, CommandSender sender) throws CommandException {
        // parts of this code are based off of mbaxter's VanishNoPacket.
        // https://github.com/mbax/VanishNoPacket/blob/master/src/org/kitteh/vanish/VanishManager.java
        OAPlayer player = controller.wrap((Player) sender);
        Field hidden = null;
        try {
            hidden = Session.class.getDeclaredField("hidden");
            hidden.setAccessible(true);
        } catch (java.lang.NoSuchFieldException e) {
            // this should NEVER happen anyway since I KNOW it exists.
            return;
        }
        int effectid = MobEffectList.INVISIBILITY.getId();
        try {
            if (hidden.getBoolean(player.getSession()) == true) {
                if (args.hasFlag('s')) player.getWorld().playEffect(player.getLocation(), Effect.SMOKE, new Random().nextInt(9));
                player.sendPacket(new Packet42RemoveMobEffect(player.getEntityId(), new MobEffect(effectid, 0, 0))); // resurface on the grid
                player.sendPacket(new Packet201PlayerInfo(player.getName(), true, player.getPing()));
                hidden.setBoolean(player.getSession(), false);
                for (final Player target : OpenAuth.getInstance().getServer().getOnlinePlayers()) {
                    OAPlayer otarget = controller.wrap(target);
                    if (target.equals(player.getPlayer())) continue;
                    if (otarget.hasPermission("openauth.admin.sight", false)) continue;
                    if (!(target.canSee(player.getPlayer()))) target.showPlayer(player.getPlayer());
                }
                player.sendMessage("You are no longer hidden!");
            } else if (hidden.getBoolean(player) == false) {
                if (args.hasFlag('s')) player.getWorld().playEffect(player.getLocation(), Effect.SMOKE, new Random().nextInt(9));
                player.sendPacket(new Packet41MobEffect(player.getEntityId(), new MobEffect(effectid, 0, 0))); // get DISAPPEARED.
                player.sendPacket(new Packet201PlayerInfo(player.getName(), false, 9999)); // hide from the status list
                hidden.setBoolean(player, true);
                for (final Player target : OpenAuth.getInstance().getServer().getOnlinePlayers()) {
                    OAPlayer otarget = controller.wrap(target);
                    if (target.equals(player.getPlayer())) continue;
                    if (otarget.hasPermission("openauth.admin.sight", false)) continue;
                    if (target.canSee(player.getPlayer())) target.hidePlayer(player.getPlayer());
                }
                player.sendMessage("You are now hidden!");
            }
        } catch (java.lang.IllegalAccessException e) {
            // this also should never happen since I'm setting the value accessible, per above.
            return;
        } finally {
            hidden.setAccessible(false);
        }
        return;
    }

    @Command(aliases = {"unhide"}, flags = "s", desc = "Unhides a player.", min = 1, max = 1)
    @CommandPermissions({ "openauth.admin.visibility.unhide" })
    public static void unhide(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = controller.wrap(args.getString(0));
        Field hidden = null;
        if (player == null) {
            sender.sendMessage(ChatColor.BLUE + args.getString(0) + " is not online!");
        }
        try {
            hidden = Session.class.getDeclaredField("hidden");
            hidden.setAccessible(true);
        } catch (java.lang.NoSuchFieldException e) {
            // this should never happen since we know for a fact that this field exists.
            return;
        }
        int effectid = MobEffectList.INVISIBILITY.getId();
        try {
            if (hidden.getBoolean(player.getSession()) == true) {
                if (args.hasFlag('s')) player.getWorld().playEffect(player.getLocation, Effect.SMOKE, new Random().nextInt(9));
                player.sendPacket(new Packet42RemoveMobEffect(player.getEntityId(), new MobEffect(effectid, 0, 0))); // resurface.
                player.sendPacket(new Packet201PlayerInfo(player.getName(), true, player.getPing()));
                hidden.setBoolean(player.getSession(), false);
                for (final Player target : OpenAuth.getInstance().getServer().getOnlinePlayers()) {
                    OAPlayer otarget = controller.wrap(target);
                    if (target.equals(player.getPlayer())) continue;
                    if (!(target.canSee(player.getPlayer()))) target.showPlayer(player.getPlayer());
                }
                player.sendMessage("You are no longer hidden!");
            } else {
                sender.sendMessage(ChatColor.BLUE + args.getString(0) + " was not hidden!");
            }
        } catch (java.lang.IllegalAccessException e) {
            // this should also never happen since the value is being set accessible, per above.
            return;
        } finally {
            hidden.setAccessible(false);
        }
    }

    @Command(aliases = {"hide"}, flags = "s", desc = "Hides a player.", min = 1, max = 1)
    @CommandPermissions({ "openauth.admin.visibility.hide" })
    public static void hide(CommandContext args, CommandSender sender) throws CommandException {
        OAPlayer player = controller.wrap(args.getString(0));
        Field hidden = null;
        if (player == null) {
            sender.sendMessage(ChatColor.BLUE + args.getString(0) + " is not online!");
        }
        try {
            hidden = Session.class.getDeclaredField("hidden");
            hidden.setAccessible(true);
        } catch (java.lang.NoSuchFieldException e) {
            // this should never happen since we know for a fact that this field exists.
            return;
        }
        int effectid = MobEffectList.INVISIBILITY.getId();
        try {
            if (hidden.getBoolean(player.getSession()) == false) {
                if (args.hasFlag('s')) player.getWorld().playEffect(player.getLocation, Effect.SMOKE, new Random().nextInt(9));
                player.sendPacket(new Packet42MobEffect(player.getEntityId(), new MobEffect(effectid, 0, 0))); // get DISAPPEARED.
                player.sendPacket(new Packet201PlayerInfo(player.getName(), false, 9999));
                hidden.setBoolean(player.getSession(), true);
                for (final Player target : OpenAuth.getInstance().getServer().getOnlinePlayers()) {
                    OAPlayer otarget = controller.wrap(target);
                    if (target.equals(player.getPlayer())) continue;
                     if (otarget.hasPermission("openauth.admin.sight", false)) continue;
                   if (target.canSee(player.getPlayer())) target.hidePlayer(player.getPlayer());
                }
                player.sendMessage("You are now hidden!");
            } else {
                sender.sendMessage(ChatColor.BLUE + args.getString(0) + " is already hidden!");
            }
        } catch (java.lang.IllegalAccessException e) {
            // this should also never happen since the value is being set accessible, per above.
            return;
        } finally {
            hidden.setAccessible(false);
        }
    }

    @Command(aliases = {"hide-all"}, flags = "s", desc = "Hides all players.")
    @CommandPermissions({ "openauth.admin.visibility.hide-all" })
    public static void hide(CommandContext args, CommandSender sender) throws CommandException {
        for (final OAPlayer player : OpenAuth.getOAServer().getLoginHandler().getActivePlayers()) {
            if (player.getSession().isHidden()) {
                sender.sendMessage("Player '" + player.getName() + "' is already hidden.");
                continue;
            }
            Field hidden = null;
            if (player == null) {
                sender.sendMessage(ChatColor.BLUE + args.getString(0) + " is not online!");
            }
            try {
                hidden = Session.class.getDeclaredField("hidden");
                hidden.setAccessible(true);
            } catch (java.lang.NoSuchFieldException e) {
                // this should never happen since we know for a fact that this field exists.
                return;
            }
            int effectid = MobEffectList.INVISIBILITY.getId();
            try {
                if (hidden.getBoolean(player.getSession()) == false) {
                    player.sendPacket(new Packet42MobEffect(player.getEntityId(), new MobEffect(effectid, 0, 0))); // get DISAPPEARED.
                    player.sendPacket(new Packet201PlayerInfo(player.getName(), false, 9999));
                    hidden.setBoolean(player.getSession(), true);
                    for (final Player target : OpenAuth.getInstance().getServer().getOnlinePlayers()) {
                        OAPlayer otarget = controller.wrap(target);
                        if (target.equals(player.getPlayer())) continue;
                        if (otarget.hasPermission("openauth.admin.sight", false)) continue;
                        if (target.canSee(player.getPlayer())) target.hidePlayer(player.getPlayer());
                    }
                    player.sendMessage("You are now hidden!");
                }
            } catch (java.lang.IllegalAccessException e) {
                // this should also never happen since the value is being set accessible, per above.
                return;
            } finally {
                hidden.setAccessible(false);
            }
        }
        return;
    }
}