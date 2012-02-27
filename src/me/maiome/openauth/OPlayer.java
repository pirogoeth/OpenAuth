package me.maiome.openauth;

// internal imports
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.Permission;
import me.maiome.openauth.util.LogHandler;

public abstract class OPlayer {

    protected OServer server;
    protected String player_ip = null;
    protected PlayerState state;

    private enum PlayerState {
        ONLINE,
        OFFLINE,
        BANNED,
        UNKNOWN
    }

    protected OPlayer(OServer server) {
        this.server = server
        this.state = PlayerState.UNKNOWN;
    }

    public abstract void getName();

    public abstract void getIP();

    public OServer getServer() {
        return this.server;
    }

    public abstract void getLocation();

    public void setLocation(int x, int y, int z) {
        setLocation(x, y, z, (double) getPitch(), (double) getYaw());
    }

    public abstract void setLocation(int x, int y, int z, double pitch, double yaw);

    public abstract double getPitch();

    public abstract double getYaw();

    public abstract boolean hasPermission(String node);

    public abstract boolean hasPermission(String node, boolean default);

    public void setOffline() {
        this.state = PlayerState.OFFLINE;
    }

    public void setOnline() {
        this.state = PlayerState.ONLINE;
    }

    public void setBanned() {
        this.state = PlayerState.BANNED;
    }

    public void setPlayerState(PlayerState state) {
        this.state = state;
    }

    public PlayerState getPlayerState() {
        return this.state;
    }
}