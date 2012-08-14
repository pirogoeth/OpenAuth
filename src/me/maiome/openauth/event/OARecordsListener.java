package me.maiome.openauth.event;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.bukkit.events.*;
import me.maiome.openauth.database.DBSessionRecord;
import me.maiome.openauth.util.*;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.*;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;

public class OARecordsListener implements Listener {

    private final LogHandler log = new LogHandler();
    private OpenAuth controller;

    public OARecordsListener(OpenAuth controller) {
        this.controller = controller;
    };

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSessionCreated(OASessionCreateEvent event) {
         DBSessionRecord sr = new DBSessionRecord(event.getSession());
         event.getSession().attachSessionRecord(sr);
         sr.save();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSessionDestroyed(OASessionDestroyEvent event) {
        DBSessionRecord sr = event.getSession().getSessionRecord();
        sr.setClosetime(System.currentTimeMillis());
        sr.update();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());
        DBSessionRecord sr = player.getSession().getSessionRecord();
        sr.setBlocksplaced(sr.getBlocksplaced() + 1L);
        sr.update();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());
        DBSessionRecord sr = player.getSession().getSessionRecord();
        sr.setBlocksdestroyed(sr.getBlocksdestroyed() + 1L);
        sr.update();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerAttemptedLogin(OAPlayerAttemptedLoginEvent event) {
        DBSessionRecord sr = event.getPlayer().getSession().getSessionRecord();
        sr.setLoginsuccess(event.loginSucceeded());
        sr.setLastloginip(event.getPlayer().getIP());
        sr.update();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());
        DBSessionRecord sr = player.getSession().getSessionRecord();
        sr.setLastlogin(System.currentTimeMillis());
        sr.setReusecount(sr.getReusecount() + 1);
        sr.update();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());
        DBSessionRecord sr = player.getSession().getSessionRecord();
        sr.setLastLocation(player.getPlayer().getLocation());
        sr.update();
    }
}