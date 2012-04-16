package me.maiome.openauth.actions;

import com.sk89q.util.StringUtil; // string util

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;

// java imports
import java.util.Iterator;
import java.util.List;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.OAServer;
import me.maiome.openauth.session.Session;
import me.maiome.openauth.session.SessionController;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.Permission;

public class SpawnStick implements IAction {

    public static final String name = "spawn";

    private String[] args = null;
    private Session attached;
    private SessionController sc;
    private final LogHandler log = new LogHandler();
    private final String permissible = "openauth.action.spawn";
    private OAServer server;
    private boolean used = false;

    protected OAPlayer sender;
    protected String creature;
    protected LivingEntity spawned;

    public SpawnStick(OAServer server, Session attached) {
        this.server = server;
        this.sc = server.getController().getSessionController();
        this.attached = attached;
        this.setSender(this.attached.getPlayer());
    }

    public String getName() {
        return name;
    }

    public boolean allowed() {
        return this.attached.getPlayer().hasPermission(this.permissible);
    }

    public boolean isUsed() {
        return this.used;
    }

    public boolean requiresEntityTarget() {
        return false;
    }

    public boolean allowsAnyEntityTarget() {
        return false;
    }

    public boolean allowsArgs() {
        return true;
    }

    public String[] getArgs() {
        return this.args;
    }

    public boolean hasArgs() {
        return (this.args == null) ? false : true;
    }

    public boolean requiresArgs() {
        return true;
    }

    public void setSender(final OAPlayer sender) {
        this.sender = sender;
    }

    public void setArgs(String[] args) {
        try {
            this.args = args;
            this.creature = args[0];
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            this.log.info(String.format("[%s] %s", e.getClass().getCanonicalName(), e.getMessage()));
            return;
        }
    }

    public void run(final OAPlayer player) {}

    public void run(final Entity entity) {}

    public void run(final Block block) {
        EntityType type = this.matchType((CommandSender) this.sender.getPlayer(), this.creature, true);
        this.spawned = block.getLocation().getWorld().spawnCreature(block.getLocation().add(0, 1, 0), type);
    }

    public void undo() {
        this.spawned.remove();
    }

    // borrowed from sk89q's commandbook
    //   @url https://github.com/sk89q/commandbook/blob/master/src/main/java/com/sk89q/commandbook/util/EntityUtil.java#L36
    public EntityType matchType(CommandSender sender, String filter, boolean spawnable) {
        for (EntityType type : EntityType.values()) {
            if (type.name().replace("_", "").equalsIgnoreCase(filter.replace("_", "")) ||
                   (type.getName() != null && type.getName().equalsIgnoreCase(filter)) && (type.isSpawnable() || !(spawnable))) {
                return type;
            }
        }

        for (EntityType testType : EntityType.values()) {
            if (testType.getName() != null && testType.getName().toLowerCase().startsWith(filter.toLowerCase()) && (testType.isSpawnable() || !(spawnable))) {
                return testType;
            }
        }

        this.sender.sendMessage("The mob you specified doesn't exist, sorry :/ but, you can choose from these mobs to spawn: " + this.getEntityTypeNameList(spawnable));
        return null;
    }

    // borrowed from sk89q's commandbook
    //   @url https://github.com/sk89q/commandbook/blob/master/src/main/java/com/sk89q/commandbook/util/EntityUtil.java#L63
    public String getEntityTypeNameList(boolean spawnable) {
        StringBuilder str = new StringBuilder();
        for (EntityType type : EntityType.values()) {
            if (!(spawnable) || type.isSpawnable()) {
                if (str.length() > 0) {
                    str.append(", ");
                }
                str.append(type.getName());
            }
        }

        return str.toString();
    }
}