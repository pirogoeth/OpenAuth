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

    @OAJSONAPIMethod(name = "oa-getWhitelist")
    public static String getWhitelist(Object[] args) {
        StringBuilder s = new StringBuilder();
        List<String> whitelist = OpenAuth.getOAServer().getWhitelistHandler().getWhitelist();
        for (String name : whitelist) {
            s.append(name + ",");
        }
        return s.toString();
    }

    @OAJSONAPIMethod(name = "oa-whitelistPlayer")
    public static String whitelistPlayer(Object[] args) {
        String name;
        try {
            name = (String) args[0];
        } catch (Exception e) {
            throw e;
        }
        OpenAuth.getOAServer().getWhitelistHandler().whitelistPlayer(name);
        // verify that the user was in fact added to the whitelist
        OpenAuth.getOAServer().getWhitelistHandler().saveWhitelist();
        OpenAuth.getOAServer().getWhitelistHandler().loadWhitelist();
        List<String> whitelist = OpenAuth.getOAServer().getWhitelistHandler().getWhitelist();
        if (whitelist.contains(name)) {
            return "true";
        }
        return "false";
    }

    @OAJSONAPIMethod(name = "oa-unwhitelistPlayer")
    public static String unwhitelistPlayer(Object[] args) {
        String name;
        try {
            name = (String) args[0];
        } catch (Exception e) {
            throw e;
        }
        OpenAuth.getOAServer().getWhitelistHandler().unwhitelistPlayer(name);
        // verify that the user was in fact removed from the whitelist
        OpenAuth.getOAServer().getWhitelistHandler().saveWhitelist();
        OpenAuth.getOAServer().getWhitelistHandler().loadWhitelist();
        List<String> whitelist = OpenAuth.getOAServer().getWhitelistHandler().getWhitelist();
        if (whitelist.contains(name)) {
            return "false";
        }
        return "true";
    }
}