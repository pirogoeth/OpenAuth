package me.maiome.openauth.commands;

// internal imports
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.mixins.MixinManager;
import me.maiome.openauth.util.*;

// command framework imports
import com.sk89q.minecraft.util.commands.*;

// bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

// java imports
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

// etCommon imports
import net.eisental.common.page.Pager;

public class OAMixinCommands {

    private static OpenAuth controller;
    private static final LogHandler log = new LogHandler();

    public OAMixinCommands(OpenAuth openauth) {
        controller = openauth;
    }

    public static class OAMixinParentCommand {

        private static OpenAuth controller;

        public OAMixinParentCommand(OpenAuth openauth) {
            controller = openauth;
        }

        @Command(aliases = {"mixin", "m"}, desc = "OpenAuth mixin management commands",
                 flags = "", min = 1)
        public static void mixin() {}
    }

    @Command(aliases = {"list"}, usage = "", desc = "Lists all loaded mixins.", max = 1, min = 1)
    @CommandPermissions({ "openauth.admin.list-mixins" })
    public static void listMixins(CommandContext args, CommandSender sender) throws CommandException {
    }

    @Command(aliases = {"load-new-mixins"}, usage = "", desc = "Loads any mixins that haven't been loaded yet.", max = 0)
    @CommandPermissions({ "openauth.admin.load-new-mixins" })
    public static void loadNewMixins(CommandContext args, CommandSender sender) throws CommandException {
        MixinManager.getInstance().load();
        sender.sendMessage(ChatColor.GREEN + "Tried to load new mixins -- check console for feedback.");
    }
}
