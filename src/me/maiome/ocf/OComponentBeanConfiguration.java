package me.maiome.ocf;

import java.lang.annotation.*;

/**
 * This annotation allows declaration of a bean information on a BEAN target ONLY.
 *
 * This annotation is NOT optional. If this annotation is not used on a BEAN target, the framework will reject
 * the component.
 *
 * Defaults WILL be provided :p
 *
 * Example of (full) usage:
 *
 *   @OComponent(name = "Example Component")
 *   @OComponentType({ ComponentType.BEAN })
 *   @OComponentBeanTarget(ExampleComponent.ExampleBean.class)
 *   @OComponentBeanConfiguration(
 *      driver = "org.sqlite.JDBC",
 *      url = "jdbc:sqlite:{DIR}/{NAME}.db" // not recommended..
 *      username = "captain",
 *      password = "narwhals",
 *      isolation = "SERIALIZABLE",
 *      logging = false,
 *      rebuild = false,
 *      wal_mode = true // don't touch this if you don't know what it is. is has the ability to blow EVERYTHING up.
 *   )
 *   public class ExampleComponent {
 *       ...
 *       @Entity
 *       public class ExampleBean implements OComponentBeanModel { ... }
 *   }
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OComponentBeanConfiguration {

    /**
     * This defines the driver to use for the connection, default "org.sqlite.JDBC".
     */
    String driver() default "org.sqlite.JDBC";

    /**
     * This defines the URL to connect to the database, default "jdbc:sqlite:{OCROOT}/{COMPONENTNAME}.db"
     */
    String url() default "jdbc:sqlite:{OCROOT}/{COMPONENTNAME}.db";

    /**
     * This defines the username to connect to the database with, default "captain".
     */
    String username() default "captain";

    /**
     * This defines the password to connect to the database with, default "narwhals".
     */
    String password() default "narwhals";

    /**
     * This defines the db isolation type, default "SERIALIZABLE".
     */
    String isolation() default "SERIALIZABLE";

    /**
     * This turns ebean's logging on or off, default false.
     */
    boolean logging() default false;

    /**
     * This tells ebean whether or not to rebuild your database on every load, default false.
     */
    boolean rebuild() default false;

    /**
     * This specifies whether or not to send a journal_mode query to the database to activate WAL mode.
     * If using SQLite, I recommend leaving this alone.
     */
    boolean wal_mode() default true;

}