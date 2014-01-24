package me.maiome.openauth.util;

// java imports
import java.util.logging.Logger;
import java.lang.Boolean;

public class LogHandler {

    // main
    public final static Logger log = Logger.getLogger("Minecraft");
    public final static String prefix = "OpenAuth";
    // dynamic
    protected static boolean debug;

    public LogHandler() {
    }

    public static void loadDebugValue() {
        debug = Config.getConfig().getBoolean("debug", false);
    }

    public static boolean getDebugging() {
        return debug;
    }

    public static void setDebugging(boolean b) {
        debug = b;
        info(String.format("Debugging is set to %s.", Boolean.toString(debug)));
    }

    @Deprecated
    public static void exDebug(String message) {
        debug(message);
    }

    public static void debug(String message) {
        if (!getDebugging()) return;
        String logged = String.format("[%s-debug] %s", prefix, message);
        log.info(logged);
    }

    // normal methods to wrap Logger

    public static void info(String message) {
        String logged = String.format("[%s] %s", prefix, message);
        log.info(logged);
    }

    public static void severe(String message) {
        String logged = String.format("[%s] %s", prefix, message);
        log.severe(logged);
    }

    public static void warning(String message) {
        String logged = String.format("[%s] %s", prefix, message);
        log.warning(logged);
    }
}