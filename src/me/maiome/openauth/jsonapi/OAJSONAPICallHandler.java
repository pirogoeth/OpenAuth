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
    // this is the runnable task to register the call handlers
    private Runnable registration_task = new Runnable() {
        public void run() {
            registerHandler();
        }
    };

    public OAJSONAPICallHandler(OpenAuth controller) {
        this.controller = controller;
        try {
            if (this.usable) {
                log.info("[OAJSONAPICallHandler] Waiting ten seconds to give the server a chance to finish loading..");
                OpenAuth.getOAServer().scheduleSyncDelayedTask(100L, this.registration_task);
                OpenAuth.setJSONAPICallHandler(this);
            }
        } catch (java.lang.NoClassDefFoundError e) {
            log.warning("JSONAPI call handler could not be loaded -- is JSONAPI loaded?");
        } catch (java.lang.Exception e) {
            log.warning("Unknown exception occurred.");
            e.printStackTrace();
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
    public void registerClass(Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        int registered = 0;
        for (Method method : methods) {
            if (!(method.isAnnotationPresent(OAJSONAPIMethod.class))) {
                continue;
            } else if (!(Modifier.isStatic(method.getModifiers()))) {
                continue; // I don't want to have to deal with non-static methods right now.
            } else { // we'll register it because: a) has OAJSONAPIMethod annotation; b) it's a static method.
                OAJSONAPIMethod anno = method.getAnnotation(OAJSONAPIMethod.class);
                String name = ((anno.name().equals("null")) ? method.getName() : anno.name());
                if (this.mmap.containsKey(name)) {
                    log.warning("Method map already contains method with the name " + name + "!");
                    continue;
                }
                this.mmap.put(name, method);
                registered++;
            }
        }
        if (registered > 0) {
            log.exDebug(String.format("[OAJSONAPICallHandler] Registered %d methods from class %s.", registered, clazz.getCanonicalName()));
        }
    }

    public class CallHandler implements JSONAPICallHandler {

        public boolean willHandle(APIMethodName method) {
            return mmap.containsKey(method.getMethodName());
        }

        public Object handle(APIMethodName method, Object[] args) {
            try {
                Method m = mmap.get(method.getMethodName());
                return m.invoke(null, new Object[]{args});
            } catch (java.lang.Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}