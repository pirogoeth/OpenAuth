package me.maiome.openauth.jsonapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OAJSONAPIMethod {

    /**
     * An alternate name to use instead of the method name.
     */
    String name() default "null";

}