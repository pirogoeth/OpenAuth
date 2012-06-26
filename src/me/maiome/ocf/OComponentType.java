package me.maiome.ocf;

import java.lang.annotation.*;

/**
 * This annotation declares the types of sub-components that make up the total component
 * that is being loaded. If no component type is declared, the default is ComponentType.SUPPORT.
 *
 * If OComponentType is not specified on the component, then the component will be rejected from the framework.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CLASS)
public @interface OComponentType {

    /**
     * Contains the component types that are used inside this component.
     */
    ComponentType[] value() default { ComponentType.SUPPORT };

}