package me.maiome.openauth.security;

import java.lang.reflect.*;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.util.LogHandler;

public enum OAPasswordSecurity {

    // enum format: CONSTANT(Class<? implements IPasswordSecurity>, String id, int rank);
    BASIC(BasicPasswordSecurity.class),
    COMPLEX(ComplexPasswordSecurity.class);

    public Class clazz;
    private final static Map<String, IPasswordSecurity> id_map = new HashMap<String, IPasswordSecurity>();
    private final static Map<String, Class> class_map = new HashMap<String, Class>();
    private final static Map<Integer, IPasswordSecurity> rank_map = new HashMap<Integer, IPasswordSecurity>();
    private final static Class[] validator_cons_types = {OAServer.class};
    private final static LogHandler log = new LogHandler();

    OAPasswordSecurity(Class clazz, String id, int rank) {
        this.clazz = clazz;
    }

    public static void registerPasswordSecurityValidator(Class validator) {
        try {
            Constructor c = validator.getConstructor(validator_cons_types);
            String name = (String) validator.getField("name").get(validator);
            int rank = (Integer) validator.getField("rank").get(validator);
            IPasswordSecurity instance = c.newInstance(OpenAuth.getServer());
            id_map.put(name, instance);
            class_map.put(name, validator);
            rank_map.put(rank, instance);
        } catch (java.lang.Exception e) {
            log.info("Exception caught while registering validator: " + validator.getCanonicalName());
            e.printStackTrace();
            return;
        }
    }

    public static void purgePasswordSecurityValidator(String name) {
        rank_map.remove(((IPasswordSecurity) id_map.remove(name)).rank);
        class_map.remove(name);
    }

    static {
        for (OAPasswordSecurity ps : OAPasswordSecurity.values()) {
            registerPasswordSecurity(ps.getClass());
        }
    }

    private static Class getValidatorClassByName(String name) {
        return class_map.get(name);
    }

    public static IPasswordSecurity getActiveSecurityValidator() {
        String active = ConfigInventory.MAIN.getConfig().getString("auth.password-security", "basic");
        return instantiate(getValidatorClassByName(active));
    }

    private static IPasswordSecurity getByRank(int rank) {
        return rank_map.get(rank);
    }

    public static IPasswordSecurity instantiate(Class validator) {
        try {
            Constructor c = validator.getConstructor(validator_cons_types);
            return (IPasswordSecurity) c.newInstance(OpenAuth.getServer());
        } catch (java.lang.Exception e) {
            log.info("Exception caught while instantiating a password validator.");
            e.printStackTrace();
            return null;
        }
    }

    public IPasswordSecurity instantiate() {
        try {
            Constructor c = this.clazz.getConstructor(validator_cons_types);
            return (IPasswordSecurity) c.newInstance(OpenAuth.getServer());
        } catch (java.lang.Exception e) {
            log.info("Exception caught while instantiating a password validator.");
            e.printStackTrace();
            return null;
        }
    }

    public Class getClass() {
        return this.clazz;
    }
}