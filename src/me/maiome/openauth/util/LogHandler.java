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
    public LogHandler () {
        // now handling logs.
        this.info(String.format("Extraneous debug is set to %s.", Boolean(this.ex_debug).toString()));
    }

    // extraneous debug methods

    public static boolean getExtraneousDebugging () {
        return this.ex_debug;
    }

    public static void setExtraneousDebugging (boolean b) {
        this.ex_debug = b;
    }

    public static void exDebug (String message) {
        this.error(this.prefix, message);
    }

    // normal methods to wrap Logger

    public void info (String message) {
        String logged = String.format("[%s] %s", this.prefix, message);
        this.log.info(logged);
    }

    public void debug (String message) {
        String logged = String.format("[%s] %s", this.prefix, message);
        this.log.debug(logged);
    }

    public void critical (String message) {
        String logged = String.format("[%s] %s", this.prefix, message);
        this.log.critical(logged);
    }

    public void warning (String message) {
        String logged = String.format("[%s] %s", this.prefix, message);
        this.log.warning(logged);
    }

    public void error (String message) {
        String logged = String.format("[%s] %s", this.prefix, message);
        this.log.error(logged);
    }
}