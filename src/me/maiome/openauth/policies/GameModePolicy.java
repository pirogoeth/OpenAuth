package me.maiome.openauth.policies;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.database.DBWorldRecord;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.*;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.*;

import java.util.*;

public class GameModePolicy implements Listener {

    private final OpenAuth controller = (OpenAuth) OpenAuth.getInstance();
    private static final LogHandler log = new LogHandler();

    public GameModePolicy() {
        this.controller.getServer().getPluginManager().registerEvents(this, OpenAuth.getInstance());
        log.exDebug("Registered GameModePolicy events.");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChangedMode(PlayerGameModeChangeEvent event) {
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());
        World w = event.getPlayer().getLocation().getWorld();
        DBWorldRecord record = DBWorldRecord.getWorldRecord(w);
        if (!(player.hasPermission(String.format("openauth.gmpolicy.exempt.%s", w.getName()))) &&
            record.getEnforce() == true && player.getPlayer().getGameMode().getValue() != record.getGamemode()) {

            event.setCancelled(true);
            player.getPlayer().setGameMode(GameMode.getByValue(record.getGamemode()));
        }
        if (!(player.hasPermission(String.format("openauth.gmpolicy.exempt.%s", w.getName()))) &&
            record.getEnforce() == true && event.getNewGameMode().getValue() != record.getGamemode()) {
            // trying to change to an incorrect mode for the world..

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());
        World w = player.getLocation().getWorld();
        DBWorldRecord record = DBWorldRecord.getWorldRecord(w);
        if (record.getLockdown() == true && !(player.hasPermission(String.format("openauth.gmpolicy.bypass.%s", w.getName())))) {
            player.getPlayer().teleport(event.getFrom().getSpawnLocation(), TeleportCause.PLUGIN);
            player.sendMessage("World " + w.getName() + " is currently on lockdown. Please try again later.");
            return;
        }
        if (!(player.hasPermission(String.format("openauth.gmpolicy.exempt.%s", w.getName()))) &&
            record.getEnforce() == true && player.getPlayer().getGameMode().getValue() != record.getGamemode()) {

            player.getPlayer().setGameMode(GameMode.getByValue(record.getGamemode()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldLoad(WorldLoadEvent event) {
        DBWorldRecord record = DBWorldRecord.getWorldRecord(event.getWorld());
        log.info("[DB] Loaded record for " + event.getWorld().getName() + ".");
    }
}