package me.maiome.openauth.jsonapi;

// jsonapi
import com.alecgorge.minecraft.jsonapi.*;
import com.alecgorge.minecraft.jsonapi.api.*;

// internal
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.util.*;

// java
import java.lang.reflect.*;
import java.util.*;

public class OAJSONAPICallHandler {

    public static boolean usable = (Permission.packageExists("com.alecgorge.minecraft.jsonapi.JSONAPI") ? true : false);
    private final LogHandler log = new LogHandler();
    private OpenAuth controller;
    private OAServer server = OpenAuth.getOAServer();
    // this is a map of method call names to methods to run for said names.
    private Map<String, Method> mmap = new HashMap<String, Method>();
    // this is a map of parent => method relations
    private Map<Method, Object> pmap = new HashMap<Method, Object>();

    public OAJSONAPICallHandler(OpenAuth controller) {
        this.controller = controller;
        if (this.usable) {
            log.info("Registering JSONAPICallHandler...");
            this.registerHandler();
            OpenAuth.setJSONAPICallHandler(this);
        }
    }

    // register this class with JSONAPI for method handling.
    protected void registerHandler() {
        JSONAPI jsonapi = (JSONAPI) this.server.getServer().getPluginManager().getPlugin("JSONAPI");
        try {
            jsonapi.registerAPICallHandler(new CallHandler());
            log.info(String.format(
                "Successfully registered JSONAPI call handler with [%s]!", jsonapi.getDescription().getFullName()));
        } catch (java.lang.NullPointerException e) {
            log.warning("NullPointerException encountered while registering JSONAPI call handler -- is JSONAPI enabled?");
            e.printStackTrace();
        } catch (java.lang.Exception e) {
            log.warning("Unknown exception occurred while registering JSONAPI call handler.");
            e.printStackTrace();
        }
    }

    // this registers a method in the map to be checked for and run later.
    public void registerMethod(String name, Method m, Object parent) {
        if (this.mmap.containsKey(name) || this.mmap.containsKey(m)) return;
        this.mmap.put(name, m);
        this.pmap.put(m, parent);
    }

    // this deregisters a method
    public void deregisterMethod(String name) {
        if (!(this.mmap.containsKey(name))) return;
        try {
            this.pmap.remove(this.mmap.get(name));
        } catch (java.lang.Exception e) {}
        this.mmap.remove(name);
    }

    public class CallHandler implements JSONAPICallHandler {

        public boolean willHandle(APIMethodName method) {
            return mmap.containsKey(method.getMethodName());
        }

        public Object handle(APIMethodName method, Object[] args) {
            try {
                Method m = mmap.get(method.getMethodName());
                Object parent = pmap.get(m);
                return m.invoke(parent, args);
            } catch (java.lang.Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}