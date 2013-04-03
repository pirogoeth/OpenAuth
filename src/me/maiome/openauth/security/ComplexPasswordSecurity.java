package me.maiome.openauth.security;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.util.*;

public class ComplexPasswordSecurity extends Reloadable implements IPasswordSecurity {

    public static final String name = "complex";

    // this pattern makes your password require a capital letter, lowercase letter, and one digit.
    protected final static Pattern pattern = Pattern.compile("(?=.*?[0-9_])(?=.*?[A-Z])(?=.*?[a-z])\\w+");
    protected OAServer server;

    private boolean active;

    public ComplexPasswordSecurity() {
        this.reload();
        this.server = OAServer.getInstance();
        this.setReloadable(this);
    }

    protected void reload() {
        this.active = ((Config.getConfig().getString("auth.password-security", "basic").equalsIgnoreCase(this.name)) ? true : false);
    }

    public boolean isActive() {
        return this.active;
    }

    public String getName() {
        return this.name;
    }

    public String explain() {
        return "your password must be longer than 8 characters and must contain AT LEAST one uppercase letter, one lowercase letter, and one digit or your password is invalid.";
    }

    public boolean validate(String password) {
        Matcher matcher = pattern.matcher(password);
        if (password.equalsIgnoreCase("<password>")) {
            return false;
        } else if (password.equalsIgnoreCase("password")) {
            return false;
        } else if (password.length() >= 8 && matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }
}