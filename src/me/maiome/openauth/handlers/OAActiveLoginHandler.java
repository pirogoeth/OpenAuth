package me.maiome.openauth.handlers;

// java imports
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

// internal imports
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.Permission;
import me.maiome.openauth.util.LogHandler;

public class OAActiveLoginHandler implements OALoginHandler {

    private List<OAPlayer> active = new ArrayList<OAPlayer>();
    private OpenAuth plugin;

    public OAActiveLoginHandler(OpenAuth plugin) {
        this.plugin = plugin;
    }

    public boolean isPlayerLoggedIn(OAPlayer player) {
        return this.active.contains(player);
    }

    public boolean isPlayerLoggedIn(String player) {
        return this.active.contains(this.plugin.wrapOAPlayer(this.plugin.getServer().getPlayer(player)));
    }
}