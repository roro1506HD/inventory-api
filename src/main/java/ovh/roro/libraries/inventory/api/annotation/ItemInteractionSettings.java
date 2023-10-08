package ovh.roro.libraries.inventory.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ItemInteractionSettings {

    boolean leftClickAir() default true;

    boolean leftClickBlock() default true;

    boolean rightClickAir() default true;

    boolean rightClickBlock() default true;

}
