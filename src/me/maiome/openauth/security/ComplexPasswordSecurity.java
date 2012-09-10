package me.maiome.openauth.security;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;

public class ComplexPasswordSecurity implements IPasswordSecurity {

    public static final String name = "complex";
    public static final int rank = 500;

    // this pattern makes your password require a capital letter, lowercase letter, and one digit.
    protected final static Pattern pattern = Pattern.compile("(?=.*?[0-9_])(?=.*?[A-Z])(?=.*?[a-z])\\w+");
    protected OAServer server;

    public ComplexPasswordSecurity(OAServer server) {
        this.server = server;
    }

    public boolean isActive() {
        String sectype = ConfigInventory.MAIN.getConfig().getString("auth.password-security", "complex");
        return (sectype.equalsIgnoreCase(this.name) ? true : false);
    }

    public String getName() {
        return this.name;
    }

    public int getRank() {
        return this.rank;
    }

    public String explain() {
        return "your password must be longer than 4 characters and must contain AT LEAST one uppercase letter, one lowercase letter, and one digit or your password is invalid.";
    }

    public boolean validate(String password) {
        Matcher matcher = pattern.matcher(password);
        if (password.length() > 4 && matcher.matches()) {
            return true;
        } else if (password.equalsIgnoreCase("<password>")) {
            return false;
        } else if (password.equalsIgnoreCase("password")) {
            return false;
        } else {
            return false;
        }
    }
}