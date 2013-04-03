package me.maiome.openauth.mixins;

import me.maiome.openauth.bukkit.OpenAuth;

public interface IMixinLibraryProvider {

    /**
     * Displayable name of the mixin library.
     */
    String name = "";

    /**
     * Returns the name of the mixin library.
     */
    String getName();

}