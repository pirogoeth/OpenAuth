package me.maiome.openauth;

// java imports
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

// internal imports
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.Permission;
import me.maiome.openauth.util.LogHandler;

public abstract class OServer {

    private final LogHandler log = new LogHandler();

    private Map<String, String> ip_bans = new HashMap<String, String>();
    private Map<String, String> name_bans = new HashMap<String, String>();

    public abstract void getServer();

    public abstract void kickPlayer(OPlayer player);

    public abstract void banPlayerByIP(OPlayer player);

    public abstract void unbanPlayerByIP(OPlayer player);

    public abstract void banPlayerByName(OPlayer player);

    public abstract void unbanPlayerByName(OPlayer player);
}