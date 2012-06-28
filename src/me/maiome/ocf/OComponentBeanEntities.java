package me.maiome.ocf;

import java.lang.annotation.*;

/**
 * This annotation is well, weird.
 *
 * Basically, what it's here for is to specify all the bean entities that you want to use in your component so the loader
 * can create the database correctly.
 *
 * If you have many different Entities, this is what you want to use, and all entities need to extend the OComponentBeanModel.
 *
 * If you don't specify this, your database will be created with ONLY the bean that your Component contains, if any.
 * So that means that if your main component is a bean, eg:
 *    @OComponent("SomeBean")
 *    @OComponentType(ComponentType.BEAN)
 *    @Entity
 *    @Table(name = "somejunk")
 *    public class SomeBean extends OComponentBeanModel { ... }
 * The loader will add "SomeBean" to the database list for ebean to work with.
 * Also, it's the same if you use a OComponentBeanTarget. If you have one declared, and no OComponentBeanEntities declared,
 * the only bean that will be loaded is the one listed in OComponentBeanTarget.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OComponentBeanEntities {

    /**
     * This declares all the component bean entities to list (may exist in other components as well.)
     */
    Class<? extends OComponentBeanModel> value();

}