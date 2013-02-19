package me.maiome.openauth.mixins;

import me.maiome.openauth.bukkit.OpenAuth;

public interface IMixin {

    /**
     * Displayable name of the mixin.
     */
    String name = "";

    /**
     * Instance of the controller.
     */
    OpenAuth controller = null;

    /**
     * Returns the name of the mixin.
     */
    String getName();

    /**
     * Called when the mixin is initialised.
     */
    void onInit();
    /**
     * Called when the mixin is being torn down.
     */
    void onTeardown();
}