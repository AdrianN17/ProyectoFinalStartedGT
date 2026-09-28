package pe.edu.galaxy.training.java.sensitive.policy;

import pe.edu.galaxy.training.java.sensitive.annotation.Mask;
import pe.edu.galaxy.training.java.sensitive.enums.SensitivityLevel;
import pe.edu.galaxy.training.java.sensitive.service.MaskService;

public class DefaultSensitivePolicy implements SensitivePolicy {

    private final MaskService maskService;

    public DefaultSensitivePolicy(MaskService maskService) {
        this.maskService = maskService;
    }

    @Override
    public Object apply(Object value, SensitivityLevel level, Mask mask) {
        if (value == null) {
            return null;
        }

        String text = value.toString();

        return switch (level) {
            case LOW -> text;

            case MEDIUM, HIGH -> {
                if (mask != null) {
                    yield maskService.mask(text, mask);
                }
                yield maskFull(text);
            }

            case CRITICAL -> "[SENSITIVE]";
        };
    }

    private String maskFull(String value) {
        return "*".repeat(value.length());
    }
}