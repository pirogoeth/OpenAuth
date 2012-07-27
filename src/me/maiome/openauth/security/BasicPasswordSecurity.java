package me.maiome.openauth.security;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.util.ConfigInventory;

public class BasicPasswordSecurity implements IPasswordSecurity {

    public static final String name = "basic";
    public static final int rank = 100;

    protected OAServer server;

    public BasicPasswordSecurity(OAServer server) {
        this.server = server;
    }

    public boolean isActive() {
        String sectype = ConfigInventory.MAIN.getConfig().getString("auth.password-security", "basic");
        return (sectype.equalsIgnoreCase(this.name) ? true : false);
    }

    public String getName() {
        return this.name;
    }

    public int getRank() {
        return this.rank;
    }

    public String explain() {
        return "your password is less than 4 characters long.";
    }

    public boolean validate(String password) {
        if (password.length() <= 4) {
            return false;
        } else if (password.length() > 4) {
            return true;
        }
        return false;
    }
}