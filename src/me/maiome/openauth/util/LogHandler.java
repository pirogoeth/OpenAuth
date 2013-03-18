package me.maiome.openauth.util;

// java imports
import java.util.logging.Logger;
import java.lang.Boolean;

public class LogHandler {

    // main
    public final static Logger log = Logger.getLogger("Minecraft");
    public final static String prefix = "OpenAuth";
    // dynamic
    protected static boolean ex_debug = false;

    // construct
    public LogHandler() {
        // now handling logs.
    }

    // extraneous debug methods

    public static boolean getExtraneousDebugging() {
        return ex_debug;
    }

    public static void setExtraneousDebugging(boolean b) {
        ex_debug = b;
        info(String.format("Extraneous debug is set to %s.", Boolean.toString(ex_debug)));
    }

    public static void exDebug(String message) {
        if (!getExtraneousDebugging()) return;
        String logged = String.format("[%s-debug] %s", prefix, message);
        log.info(logged);
    }

    public static void debug(String message) {
        if (!getExtraneousDebugging()) return;
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