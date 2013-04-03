package me.maiome.openauth.jsonapi;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.jsonapi.*;
import me.maiome.openauth.util.*;

import java.util.*;

public class OAJSONAPINativeMethods {

    private static final LogHandler log = new LogHandler();

    public static void load() {
        try {
            OAJSONAPICallHandler.getInstance().registerClass(OAJSONAPINativeMethods.class);
        } catch (java.lang.Exception e) {
            log.info("[OAJSONAPINativeMethods] Error registering class with JSONAPI call handler -- is JSONAPI loaded?");
            e.printStackTrace();
            return;
        }
        return;
    }

    public OAJSONAPINativeMethods() { } // don't instantiate!

    @OAJSONAPIMethod(name = "oa-getWhitelist")
    public static String getWhitelist(Object[] args) {
        StringBuilder s = new StringBuilder();
        List<String> whitelist = OAServer.getInstance().getWhitelistHandler().getWhitelist();
        for (String name : whitelist) {
            s.append(name + ",");
        }
        return s.toString();
    }

    @OAJSONAPIMethod(name = "oa-whitelistPlayer")
    public static String whitelistPlayer(Object[] args) throws Exception {
        String name;
        try {
            name = (String) args[0];
        } catch (Exception e) {
            throw e;
        }
        OAServer.getInstance().getWhitelistHandler().whitelistPlayer(name);
        // verify that the user was in fact added to the whitelist
        OAServer.getInstance().getWhitelistHandler().saveWhitelist();
        OAServer.getInstance().getWhitelistHandler().loadWhitelist();
        List<String> whitelist = OAServer.getInstance().getWhitelistHandler().getWhitelist();
        if (whitelist.contains(name)) {
            return "true";
        }
        return "false";
    }

    @OAJSONAPIMethod(name = "oa-unwhitelistPlayer")
    public static String unwhitelistPlayer(Object[] args) throws Exception {
        String name;
        try {
            name = (String) args[0];
        } catch (Exception e) {
            throw e;
        }
        OAServer.getInstance().getWhitelistHandler().unwhitelistPlayer(name);
        // verify that the user was in fact removed from the whitelist
        OAServer.getInstance().getWhitelistHandler().saveWhitelist();
        OAServer.getInstance().getWhitelistHandler().loadWhitelist();
        List<String> whitelist = OAServer.getInstance().getWhitelistHandler().getWhitelist();
        if (whitelist.contains(name)) {
            return "false";
        }
        return "true";
    }
}
