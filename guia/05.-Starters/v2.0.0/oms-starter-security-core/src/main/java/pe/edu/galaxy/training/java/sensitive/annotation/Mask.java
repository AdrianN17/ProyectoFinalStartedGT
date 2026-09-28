package pe.edu.galaxy.training.java.sensitive.annotation;

import pe.edu.galaxy.training.java.sensitive.enums.MaskType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mask {

    MaskType type() default MaskType.PARTIAL;

    int visibleStart() default 2;

    int visibleEnd() default 4;

    String maskChar() default "*";
}
