package me.maiome.openauth.commands;

// internal imports
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.mixins.MixinManager;
import me.maiome.openauth.util.*;

// command framework imports
import com.sk89q.minecraft.util.commands.*;

// bukkit imports
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

public class OARootAliasCommands {

    private static OpenAuth controller;
    private static final LogHandler log = new LogHandler();

    public OARootAliasCommands (OpenAuth openauth) {
        controller = openauth;
    }

    public static class LoginRootAliasCommand {

        private static OpenAuth controller;

        public LoginRootAliasCommand (OpenAuth openauth) {
            controller = openauth;
        }

        @Command(aliases = {"login"}, desc = "Identify with OA.",
                 flags = "", min = 1, max = 1)
        public static void loginRootAlias(CommandContext args, CommandSender sender) throws CommandException {
            OACommands.login(args, sender);
        }
    }

    public static class LogoutRootAliasCommand {

        private static OpenAuth controller;

        public LogoutRootAliasCommand (OpenAuth openauth) {
            controller = openauth;
        }

        @Command(aliases = {"logout"}, desc = "Logout of OA.",
                 flags = "", max = 0)
        public static void logoutRootAlias(CommandContext args, CommandSender sender) throws CommandException {
            OACommands.logout(args, sender);
        }
    }

    public static class RegisterRootAliasCommand {

        private static OpenAuth controller;

        public RegisterRootAliasCommand (OpenAuth openauth) {
            controller = openauth;
        }

        @Command(aliases = {"register"}, desc = "Register with OA.",
                 flags = "", min = 1, max = 1)
        public static void registerRootAlias(CommandContext args, CommandSender sender) throws CommandException {
            OACommands.register(args, sender);
        }
    }
}
