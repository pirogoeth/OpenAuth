package me.maiome.openauth.event;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.bukkit.event.*;
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
    private final OpenAuth controller = OpenAuth.getInstance();

    public OARecordsListener() { };

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSessionCreated(OASessionCreateEvent event) {
         DBSessionRecord sr = new DBSessionRecord(event.getOAPlayer());
         event.getSession().attachRecord(sr);
         sr.save();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSessionDestroyed(OASessionDestroyEvent event) {
        OAPlayer player = event.getSession().getOAPlayer();
        DBSessionRecord sr = player.getSessionRecord();
        sr.setCloseTime(System.currentTimeMillis());
        sr.update();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());
        DBSessionRecord sr = player.getSessionRecord();
        sr.setBlocksPlaced(sr.getBlocksPlaced()++);
        sr.update();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer()));
        DBSessionRecord sr = player.getSessionRecord();
        sr.setBlocksDestroyed(sr.getBlocksDestroyed()++);
        sr.update();
    }
}