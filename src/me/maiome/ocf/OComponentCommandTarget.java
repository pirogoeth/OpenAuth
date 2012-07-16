package me.maiome.ocf;

import java.lang.annotation.*;

/**
 * This annotation allows declaration of a target other than the main component class as a COMMAND target.
 *
 * This annotation is optional, but if not used, the main component class will be registered with the command handling
 * system as-is.
 *
 * Example of usage:
 *
 *   @OComponent(name = "Example Component")
 *   @OComponentType({ ComponentType.COMMAND })
 *   @OComponentCommandTarget(ExampleComponent.ExampleCommands.class)
 *   public class ExampleComponent {
 *       ...
 *       public class ExampleCommands {
 *           public class ExampleNestedCommands {
 *               @Command( ... )
 *               @CommandPermissions( ... )
 *               public static void version(CommandContext args, CommandSender sender) { ... };
 *           }
 *
 *           @Command(aliases = {"example"}, desc = "Example command", flags = "x",
 *                    usage = "<name>", min = 1, max = 1)
 *           @NestedCommands({ ExampleNestedCommands.class })
 *           public static void example(CommandContext args, CommandSender sender) {};
 *       }
 *   }
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OComponentCommandTarget {

    /**
     * This value defines the target command class for the component.
     */
    Class value();
}