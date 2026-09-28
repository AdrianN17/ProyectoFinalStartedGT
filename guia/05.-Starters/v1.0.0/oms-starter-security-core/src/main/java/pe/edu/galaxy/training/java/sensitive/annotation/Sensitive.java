package pe.edu.galaxy.training.java.sensitive.annotation;

import pe.edu.galaxy.training.java.sensitive.enums.SensitivityLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {

    SensitivityLevel level() default SensitivityLevel.MEDIUM;

    String category() default "GENERAL";
}
