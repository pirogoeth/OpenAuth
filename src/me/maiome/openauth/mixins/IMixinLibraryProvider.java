package me.maiome.openauth.mixins;

import me.maiome.openauth.bukkit.OpenAuth;

public interface IMixinLibraryProvider {

    /**
     * Displayable name of the mixin.
     */
    String name = "";

    /**
     * Returns the name of the mixin.
     */
    String getName();

}