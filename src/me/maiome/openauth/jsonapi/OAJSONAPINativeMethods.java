package me.maiome.openauth.jsonapi;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.jsonapi.*;
import me.maiome.openauth.util.*;

public class OAJSONAPINativeMethods {

    private static OAJSONAPICallHandler callhandler;
    private static final LogHandler log = new LogHandler();

    public static void load() {
        try {
            OpenAuth.getJSONAPICallHandler().registerClass(OAJSONAPINativeMethods.class);
        } catch (java.lang.Exception e) {
            log.info("[OAJSONAPINativeMethods] Error registering class with JSONAPI call handler -- is JSONAPI loaded?");
            e.printStackTrace();
            return;
        }
        return;
    }

    public OAJSONAPINativeMethods() { } // don't instantiate!

    @OAJSONAPIMethod(name = "oa-test")
    public static String nothing(Object[] args) {
        StringBuilder s = new StringBuilder();
        s.append("something");
        for (Object o : args) {
            s.append(o.toString());
        }
        return s.toString();
    }
}