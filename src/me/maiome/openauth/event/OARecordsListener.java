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
    public void onPlayerSessionCreated(OASessionCreatedEvent event) {
         DBSessionRecord sr = new DBSessionRecord(event.getOAPlayer());
         event.getSession().attachRecord(sr);
         sr.save();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSessionDestroyed(OASessionCreatedEvent event) {
        DBSessionRecord sr = event.getSession().setCloseTime(System.currentTimeMillis());
        sr.update();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) { };

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) { };
}