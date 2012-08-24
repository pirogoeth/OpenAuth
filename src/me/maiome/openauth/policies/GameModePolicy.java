package me.maiome.openauth.policies;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.database.DBWorldRecord;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.*;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

import java.util.*;

public class GameModePolicy {

    private final OpenAuth controller = (OpenAuth) OpenAuth.getInstance();
    private static final LogHandler log = new LogHandler();

    public GameModePolicy() {
    }
}