import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.database.DBWorldRecord;
import me.maiome.openauth.mixins.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.*;

import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.*;

import com.sk89q.minecraft.util.commands.*;

import java.util.*;

public class GameModePolicy extends AbstractMixin {

    public GameModePolicy() {
        super("GameModePolicy");
    }

    public GameModePolicy(OpenAuth controller) {
        this();
    }

    public void onInit() {
        this.controller.getServer().getPluginManager().registerEvents(this, OpenAuth.getInstance());
        this.log.exDebug("Registered GameModePolicy events.");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChangedMode(PlayerGameModeChangeEvent event) {
        OAPlayer player = OAPlayer.getPlayer(event.getPlayer());
        World w = event.getPlayer().getLocation().getWorld();
        DBWorldRecord record = DBWorldRecord.getWorldRecord(w);
        if (event.getNewGameMode().getValue() != record.getGamemode()) {
            if (!(player.hasPermission(String.format("openauth.gmpolicy.exempt.%s", w.getName()))) && record.getEnforce() == true) {
                event.setCancelled(true);
                player.sendMessage("Your gamemode could not be changed, as this world does not permit your mode change.");
                player.getPlayer().setGameMode(GameMode.getByValue(record.getGamemode())); // TODO - there's some weirdness right here. need to queue for changes.
            }
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
        this.log.info("[DB] Loaded record for " + event.getWorld().getName() + ".");
    }

    @Console
    @Command(aliases = {"gamepolicy"}, desc = "Manages per-world game mode policy.", max = 0, flags = "ls")
    public void gmp(CommandContext args, CommandSender sender) {
        if (args.hasFlag('l')) {
            List<DBWorldRecord> records = this.controller.getDatabase().find(DBWorldRecord.class).findList();
            StringBuilder s = new StringBuilder();
            s.append("\u00A7b== World Records (" + records.size() + ") ==\u00A7f\n");
            for (DBWorldRecord record : records) {
                String f = String.format(" - %s: \u00A7aGame Mode: \u00A7b%d, \u00A7aEnforce?: \u00A7b%s, \u00A7aLocked Down?: \u00A7b%s\u00A7f\n",
                    record.getName(),
                    record.getGamemode(),
                    (record.getEnforce() == true ? "yes" : "no"),
                    (record.getLockdown() == true ? "yes" : "no"));
                s.append(f);
            }
            sender.sendMessage(s.toString());
        } else if (args.hasFlag('s')) {
            String worldName = args.getString(0);
            String property = "";
            try {
                property = args.getString(1);
            } catch (java.lang.Exception e) {
                sender.sendMessage("\u00A7bAccepted property values: \u00A7agamemode\u00A7f, \u00A7aenforce\u00A7f, \u00A7alockdown\u00A7f.");
                return;
            }
            String value = "";
            try {
                value = args.getString(2);
            } catch (java.lang.ArrayIndexOutOfBoundsException e) { }
            DBWorldRecord record = this.controller.getDatabase().find(DBWorldRecord.class, worldName);
            if (record == null) {
                sender.sendMessage("World " + worldName + " does not have an associated record.");
                return;
            }
            if ("GAMEMODE".startsWith(property.toUpperCase())) {
                if (value.equals("")) {
                    sender.sendMessage(worldName + " has a gamemode of: " + record.getGamemode());
                    return;
                }
                int mode = Integer.valueOf(value);
                if (mode != 0 && mode != 1 && mode != 2) {
                    sender.sendMessage("Invalid game mode: " + value);
                    return;
                }
                record.setGamemode(mode);
                record.update();
                sender.sendMessage("Set gamemode of " + worldName + " to " + value + ".");
                return;
            } else if ("ENFORCE".startsWith(property.toUpperCase())) {
                if (value.equals("")) {
                    sender.sendMessage(worldName + " has a enforce value of: " + record.getEnforce());
                    return;
                }
                if (!(value.toUpperCase().equals("TRUE")) && !(value.toUpperCase().equals("FALSE"))) {
                    sender.sendMessage("Invalid value for property " + property + ". Must be true or false.");
                    return;
                }
                boolean flag = Boolean.valueOf(value);
                record.setEnforce(flag);
                record.update();
                sender.sendMessage("Set enforce property of " + worldName + " to " + value + ".");
                return;
            } else if ("LOCKDOWN".startsWith(property.toUpperCase())) {
                if (value.equals("")) {
                    sender.sendMessage(worldName + " has a lockdown value of: " + record.getLockdown());
                    return;
                }
                if (!(value.toUpperCase().equals("TRUE")) && !(value.toUpperCase().equals("FALSE"))) {
                    sender.sendMessage("Invalid value for property " + property + ". Must be true or false.");
                    return;
                }
                boolean flag = Boolean.valueOf(value);
                record.setLockdown(flag);
                record.update();
                sender.sendMessage("Set lockdown property of " + worldName + " to " + value + ".");
                return;
            }
            sender.sendMessage("Property must be one of the following: lockdown, enforce, or gamemode.");
            return;
        }
    }
}