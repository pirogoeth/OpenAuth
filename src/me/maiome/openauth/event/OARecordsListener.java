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
        sr.setCloseTime(System.currentTimeMillis());
        sr.update();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());
        DBSessionRecord sr = player.getSession().getSessionRecord();
        sr.setBlocksPlaced(sr.getBlocksPlaced() + 1L);
        sr.update();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());
        DBSessionRecord sr = player.getSession().getSessionRecord();
        sr.setBlocksDestroyed(sr.getBlocksDestroyed() + 1L);
        sr.update();
    }
}