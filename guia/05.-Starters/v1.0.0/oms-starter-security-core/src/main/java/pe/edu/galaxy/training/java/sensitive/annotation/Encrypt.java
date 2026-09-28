package pe.edu.galaxy.training.java.sensitive.annotation;

import pe.edu.galaxy.training.java.sensitive.enums.EncryptionProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Encrypt {

    EncryptionProvider provider() default EncryptionProvider.DEFAULT;
}
