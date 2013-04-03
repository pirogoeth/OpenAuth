package me.maiome.openauth.security;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.util.*;

public class BasicPasswordSecurity extends Reloadable implements IPasswordSecurity {

    public static final String name = "basic";

    protected OAServer server;

    private boolean active;

    public BasicPasswordSecurity() {
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
        return "your password is less than 8 characters long or is invalid.";
    }

    public boolean validate(String password) {
        if (password.equalsIgnoreCase("<password>")) {
            return false;
        } else if (password.equalsIgnoreCase("password")) {
            return false;
        } else if (password.length() < 8) {
            return false;
        } else if (password.length() >= 8) {
            return true;
        }
        return false;
    }
}