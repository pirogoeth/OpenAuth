package me.maiome.ocf;

import java.lang.annotation.*;

/**
 * This annotation is used to define that a class is to be loaded with the OComponent framework.
 *
 * If this annotation doesn't exist on a listed class, the class will be rejected from the framework.
 *
 * This annotation has a single value, which is the friendly name of the component.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OComponent {

    /**
     * Specifies the name of the component.
     */
    String value() default "[unnamed]";

}