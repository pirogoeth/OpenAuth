package me.maiome.openauth.commands;

// internal imports
import me.maiome.openauth.bukkit.OpenAuth;
import me.maiome.openauth.bukkit.OAPlayer;
import me.maiome.openauth.bukkit.events.*;
import me.maiome.openauth.database.DBSessionRecord;
import me.maiome.openauth.util.Config;
import me.maiome.openauth.util.LogHandler;
import me.maiome.openauth.util.ConfigInventory;

// command framework imports
import com.sk89q.minecraft.util.commands.*;

// bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// etCommon imports
import net.eisental.common.page.Pager;

public class OARecordCommands {

    private static OpenAuth controller;
    private static final LogHandler log = new LogHandler();

    public static class RecordParentCommand {

        private final OpenAuth controller;

        public RecordParentCommand (OpenAuth openauth) {
            controller = openauth;
        }

        @Command(aliases = {"record", "w"}, desc = "OpenAuth record management commands",
                 flags = "", min = 1)
        @NestedCommand({OARecordCommands.class})
        public static void openAuth() {}
    }

    public OARecordCommands (OpenAuth openauth) {
        controller = openauth;
    }

    public static Class getParent() {
        return RecordParentCommand.class;
    }

    @Console
    @Command(aliases = {"findbyname"}, usage = "<name>", desc = "Finds records by name",
             min = 1, max = 1)
    @CommandPermissions({ "openauth.record.findbyname" })
    public static void findbyname(CommandContext args, CommandSender sender) throws CommandException {
        List<DBSessionRecord> records =
          OpenAuth.getInstance().getDatabase()
            .find(DBSessionRecord.class)
            .where()
              .icontains("name", args.getString(0))
            .orderBy("last_login")
            .setMaxRows(5)
            .findList();
        List<String> data = new ArrayList<String>();
        data.add("#-----------------------------------------#");
        data.add("| search for name: " + args.getString(0));
        data.add("#-----------------------------------------#");
        for (DBSessionRecord record : records) {
            Location loc = record.getLastLocationAsLoc();
            String s = String.format(
                "| Id: %d => lastlogin (%d), succeeded (%b), reuse (%d), start (%d), end (%d)\n" +
                "|-- placed (%d), destroyed (%d), x (%d), y (%d), z (%d), world (%s)",
                  record.getId(), record.getLastlogin(), record.getLoginsuccess(), record.getReusecount(),
                  record.getStarttime(), record.getClosetime(), record.getBlocksplaced(), record.getBlocksdestroyed(),
                  loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName()
            );
            data.add(s);
        }
        data.add("|                                         |");
        data.add("#-----------------------------------------#");
        for (String s : data) {
            sender.sendMessage(s);
        }
    }
}