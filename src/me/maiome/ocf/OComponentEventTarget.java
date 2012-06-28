package me.maiome.ocf;

import java.lang.annotation.*;

/**
 * This annotation allows declaration of a target other than the main component class as an EVENT target.
 *
 * This annotation is optional, but if not used, the main component class will be registered with the command handling
 * system as-is.
 *
 * Example of usage:
 *
 *   @OComponent(name = "Example Component")
 *   @OComponentType({ ComponentType.EVENT })
 *   @OComponentCommandTarget(ExampleComponent.ExampleEventListener.class)
 *   public class ExampleComponent {
 *       ...
 *       public class ExampleEventListener implements OComponentEventModel {
 *           public void onBlockBreak(BlockBreakEvent event) { ... }
 *           public void onBlockBuild(BlockBuildEvent event) { ... }
 *       }
 *   }
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OComponentEventTarget {

    /**
     * This value defines the target event listener class for the component.
     */
    Class value();

}