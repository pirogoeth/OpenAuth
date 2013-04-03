package me.maiome.openauth.security;

import java.lang.reflect.*;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.util.*;

public enum OAPasswordSecurity {

    // enum format: CONSTANT(Class<? implements IPasswordSecurity>);
    BASIC(BasicPasswordSecurity.class),
    COMPLEX(ComplexPasswordSecurity.class);

    public final Class clazz;
    private final static Map<String, IPasswordSecurity> id_map = new HashMap<String, IPasswordSecurity>();
    private final static Map<String, Class> class_map = new HashMap<String, Class>();
    private final static Class[] validator_cons_types = {};
    private final static LogHandler log = new LogHandler();

    OAPasswordSecurity(final Class clazz) {
        this.clazz = clazz;
    }

    public static void registerPasswordSecurityValidator(final Class validator) {
        try {
            String name = (String) validator.getField("name").get(validator);
            Constructor c = validator.getConstructor(validator_cons_types);
            IPasswordSecurity instance = (IPasswordSecurity) c.newInstance(OAServer.getInstance());
            id_map.put(name, instance);
            class_map.put(name, validator);
            log.debug("Registered password security validator " + validator.getCanonicalName() + ", name: " + name);
        } catch (java.lang.Exception e) {
            log.info("Exception caught while registering validator: " + validator.getCanonicalName());
            e.printStackTrace();
            return;
        }
    }

    public static void purgePasswordSecurityValidator(String name) {
        id_map.remove(name);
        class_map.remove(name);
    }

    static {
        for (OAPasswordSecurity ps : OAPasswordSecurity.values()) {
            registerPasswordSecurityValidator(ps.getValidator());
        }
    }

    private static IPasswordSecurity getValidatorByName(String name) {
        return id_map.get(name);
    }

    public static IPasswordSecurity getActiveSecurityValidator() {
        String active = Config.getConfig().getString("auth.password-security", "basic");
        return getValidatorByName(active);
    }

    public static IPasswordSecurity instantiate(final Class validator) {
        try {
            Constructor c = validator.getConstructor(validator_cons_types);
            return (IPasswordSecurity) c.newInstance();
        } catch (java.lang.Exception e) {
            log.info("Exception caught while instantiating a password validator.");
            e.printStackTrace();
            return null;
        }
    }

    public Class getValidator() {
        return this.clazz;
    }

    public String getName() {
        try {
            return (String) this.clazz.getField("name").get(this.clazz);
        } catch (java.lang.Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public IPasswordSecurity instantiate() {
        if (id_map.containsKey(this.getName())) return id_map.get(this.getName());
        try {
            Constructor c = this.clazz.getConstructor(validator_cons_types);
            return (IPasswordSecurity) c.newInstance(OAServer.getInstance());
        } catch (java.lang.Exception e) {
            log.info("Exception caught while instantiating a password validator.");
            e.printStackTrace();
            return null;
        }
    }
}