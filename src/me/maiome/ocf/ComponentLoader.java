package me.maiome.ocf;

import java.lang.annotation.*;
import java.lang.reflection.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sk89q.bukkit.util.*;
import com.sk89q.util;
import com.sk89q.minecraft.util.commands.*;

import me.maiome.openauth.database.ExtendedDB;

/**
 * This is the actual component loader code, obviously.
 * All the component checking and registering will go on in here.
 */

public class ComponentLoader {

    private CommandsManager<CommandSender> cmgr;
    private CommandsManagerRegistration cmgreg;
    private ExtendedDB database;
    private Object log = new Object() {
        private final Logger logger = Logger.getLogger("Minecraft");
        private final String prefix = "OComponentLoader";
        public void info(String...messages) {
            for (String str : messages) {
                logger.info(String.format("[%s] %s", prefix, str));
            }
        }
        public void warning(String...messages) {
            for (String str : messages) {
                logger.warning(String.format("[%s] %s", prefix, str));
            }
        }
        public void error(String...messages) {
            for (String str : messages) {
                logger.error(String.format("[%s] %s", prefix, str));
            }
        }
    };
}    