package me.maiome.ocf;

import java.lang.annotation.*;

/**
 * This annotation allows declaration of a target other than the main component class as a BEAN target.
 *
 * This annotation is optional, but if not used, the main component class will be registered with the command handling
 * system as-is.
 *
 * Example of usage:
 *
 *   @OComponent(name = "Example Component")
 *   @OComponentType({ ComponentType.BEAN })
 *   @OComponentCommandTarget(ExampleComponent.ExampleBean.class)
 *   public class ExampleComponent {
 *       ...
 *       @Entity
 *       public class ExampleBean implements OComponentBeanModel { ... }
 *   }
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CLASS)
public @interface OComponentBeanTarget {

    /**
     * This value defines the target bean class for the component.
     */
    Class value() default null;

}