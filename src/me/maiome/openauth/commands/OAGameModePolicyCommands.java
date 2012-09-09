package me.maiome.openauth.commands;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.database.*;
import me.maiome.openauth.session.*;
import me.maiome.openauth.util.*;

// command framework imports
import com.sk89q.minecraft.util.commands.*;

import org.bukkit.*;
import org.bukkit.command.CommandSender;

import java.util.*;

public class OAGameModePolicyCommands {

    private OpenAuth controller;

    public OAGameModePolicyCommands(OpenAuth controller) {
        this.controller = controller;
    }

    public static class GMPParentCommand {

        private OpenAuth controller;

        public GMPParentCommand(OpenAuth controller) {
            this.controller = controller;
        }

        @Command(aliases = {"gmp"}, desc = "Commands to control the game mode policy.")
        @NestedCommand({ OAGameModePolicyCommands.class })
        public static void gmp() { }
    }

    @Console
    @Command(aliases = {"list"}, desc = "List the world records and their properties.", max = 0)
    @CommandPermissions({ "openauth.gmp.list" })
    public void list(CommandContext args, CommandSender sender) {
        List<DBWorldRecord> records = controller.getDatabase().find(DBWorldRecord.class).findList();
        StringBuilder s = new StringBuilder();
        s.append("\u00A7b== World Records (" + records.size() + ") ==\u00A7f\n");
        for (DBWorldRecord record : records) {
            String f = String.format("%s: game mode: %d, enforce policies: %s, locked down: %s\n",
                record.getName(),
                record.getGamemode(),
                (record.getEnforce() == true ? "yes" : "no"),
                (record.getLockdown() == true ? "yes" : "no"));
            s.append(f);
        }
        sender.sendMessage(s.toString());
    }

    @Console
    @Command(aliases = {"set"}, desc = "Set a record value.", usage = "<world> <property> [value]", min = 2, max = 3)
    @CommandPermissions({ "openauth.gmp.set" })
    public void setproperty(CommandContext args, CommandSender sender) {
        String worldName = args.getString(0);
        String property = args.getString(1);
        String value = "";
        try {
            value = args.getString(2);
        } catch (java.lang.ArrayIndexOutOfBoundsException e) { }
        DBWorldRecord record = controller.getDatabase().find(DBWorldRecord.class, worldName);
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
