package me.maiome.openauth.jsonapi;

// internals
import me.maiome.openauth.bukkit.*;
import me.maiome.openauth.jsonapi.OAJSONAPICallHandler;
import me.maiome.openauth.util.*;

// java
import java.lang.reflect.*;

public class JSONAPITestMethod {

    public OpenAuth controller;
    private OAJSONAPICallHandler jsch;
    private final LogHandler log = new LogHandler();

    public JSONAPITestMethod(OpenAuth controller) {
        this.controller = controller;
        this.jsch = this.controller.getJSONAPICallHandler();
        if (this.jsch == null) {
            log.info("OAJSONAPICallHandler is not initialised -- is JSONAPI loaded?");
        } else {
            try {
                this.jsch.registerMethod(
                    "test-1",
                    this.getClass().getMethod("testone", Object.class),
                    this);
                log.info("Registered test method!");
            } catch (java.lang.Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String testone(Object[] args) {
        log.info("Successfully ran JSON test method!");
        return "test";
    }
}