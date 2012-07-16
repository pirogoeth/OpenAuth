package me.maiome.ocf;

/**
 * This enum defines several different component types to use inside the framework.
 *
 * EVENT defines that the component contains an event class to be loaded into the Bukkit
 *   event framework.
 * COMMAND defines that the component contains a command class or command definitions to be
 *   registered in sk89q's command framework system.
 * BEAN defines thats the component is a bean class to be used with ebean.
 *   (these don't do much on their own, they'll most likely be used by other components for data storage.)
 *
 * Component types can be used alone or together in the @OComponentType annotation.
 */

public enum ComponentType {

    EVENT,
    COMMAND,
    BEAN;
}