package pe.edu.galaxy.training.java.sensitive.policy;

import pe.edu.galaxy.training.java.sensitive.annotation.Mask;
import pe.edu.galaxy.training.java.sensitive.enums.SensitivityLevel;

public interface SensitivePolicy {

    Object apply(
            Object value,
            SensitivityLevel level,
            Mask mask);

}