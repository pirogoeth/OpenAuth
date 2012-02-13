package me.maiome.openauth.util;

import me.maiome.openauth.util.LogHandler;

public class CommandException extends Exception {
    protected static String trace;
    public LogHandler log = new LogHandler();
    public CommandException () {
        super();
        trace = "Unknown sub-exception handled.";
        log.warning("[OpenAuth{Command}] " + trace);
    }

    public CommandException (String error) {
        super(error);
        trace = error;
        log.warning("[OpenAuth{Command}] " + trace);
    }

    public static String getError () {
        return trace;
    }
}