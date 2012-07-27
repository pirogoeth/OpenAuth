
package me.maiome.openauth.security;

import java.lang.reflect.*;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.util.ConfigInventory;
import me.maiome.openauth.util.LogHandler;

public enum OAPasswordSecurity {

    // enum format: CONSTANT(Class<? implements IPasswordSecurity>);
    BASIC(BasicPasswordSecurity.class),
    COMPLEX(ComplexPasswordSecurity.class);

    public final Class clazz;
    private final static Map<String, IPasswordSecurity> id_map = new HashMap<String, IPasswordSecurity>();
    private final static Map<String, Class> class_map = new HashMap<String, Class>();
    private final static Map<Integer, String> rank_map = new HashMap<Integer, String>();
    private final static Class[] validator_cons_types = {OAServer.class};
    private final static LogHandler log = new LogHandler();

    OAPasswordSecurity(final Class clazz) {
        this.clazz = clazz;
    }

    public static void registerPasswordSecurityValidator(final Class validator) {
        try {
            String name = (String) validator.getField("name").get(validator);
            int rank = (Integer) validator.getField("rank").get(validator);
            Constructor c = validator.getConstructor(validator_cons_types);
            IPasswordSecurity instance = (IPasswordSecurity) c.newInstance(OpenAuth.getOAServer());
            id_map.put(name, instance);
            class_map.put(name, validator);
            rank_map.put(rank, name);
            log.exDebug("Registered password security validator " + validator.getCanonicalName() + ", name: " + name + ", rank: " + rank);
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
            registerPasswordSecurityValidator(ps.getValidator());
        }
    }

    private static IPasswordSecurity getValidatorByName(String name) {
        return id_map.get(name);
    }

    public static IPasswordSecurity getActiveSecurityValidator() {
        String active = ConfigInventory.MAIN.getConfig().getString("auth.password-security", "basic");
        return getValidatorByName(active);
    }

    private static IPasswordSecurity getByRank(int rank) {
        return id_map.get(rank_map.get(rank));
    }

    public static IPasswordSecurity instantiate(final Class validator) {
        try {
            Constructor c = validator.getConstructor(validator_cons_types);
            return (IPasswordSecurity) c.newInstance(OpenAuth.getOAServer());
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

    public int getRank() {
        try {
            return (Integer) this.clazz.getField("rank").get(this.clazz);
        } catch (java.lang.Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public IPasswordSecurity instantiate() {
        if (id_map.containsKey(this.getName())) return id_map.get(this.getName());
        try {
            Constructor c = this.clazz.getConstructor(validator_cons_types);
            return (IPasswordSecurity) c.newInstance(OpenAuth.getOAServer());
        } catch (java.lang.Exception e) {
            log.info("Exception caught while instantiating a password validator.");
            e.printStackTrace();
            return null;
        }
    }
}