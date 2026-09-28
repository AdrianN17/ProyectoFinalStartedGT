package pe.edu.galaxy.training.java.sensitive.service.impl;

import pe.edu.galaxy.training.java.sensitive.annotation.Mask;
import pe.edu.galaxy.training.java.sensitive.enums.MaskType;
import pe.edu.galaxy.training.java.sensitive.service.MaskService;

public class DefaultMaskService implements MaskService {

    @Override
    public String mask(String value, Mask mask) {
        if (value == null || value.isBlank()) {
            return value;
        }

        MaskType type = mask.type();

        return switch (type) {
            case EMAIL -> maskEmail(value, mask.maskChar());
            case PHONE -> maskKeepEnd(value, mask.visibleEnd(), mask.maskChar());
            case DOCUMENT -> maskKeepEnd(value, mask.visibleEnd(), mask.maskChar());
            case CARD -> maskCard(value, mask.maskChar());
            case FULL -> repeat(mask.maskChar(), value.length());
            case PARTIAL -> maskPartial(value, mask.visibleStart(), mask.visibleEnd(), mask.maskChar());
        };
    }

    private String maskEmail(String value, String maskChar) {
        int atIndex = value.indexOf("@");
        if (atIndex <= 1) {
            return maskPartial(value, 1, 0, maskChar);
        }

        String local = value.substring(0, atIndex);
        String domain = value.substring(atIndex);

        String maskedLocal = local.substring(0, Math.min(2, local.length()))
                + repeat(maskChar, Math.max(1, local.length() - 2));

        return maskedLocal + domain;
    }

    private String maskCard(String value, String maskChar) {
        String clean = value.replace(" ", "");
        return maskKeepEnd(clean, 4, maskChar);
    }

    private String maskKeepEnd(String value, int visibleEnd, String maskChar) {
        if (value.length() <= visibleEnd) {
            return repeat(maskChar, value.length());
        }

        return repeat(maskChar, value.length() - visibleEnd)
                + value.substring(value.length() - visibleEnd);
    }

    private String maskPartial(String value, int visibleStart, int visibleEnd, String maskChar) {
        if (value.length() <= visibleStart + visibleEnd) {
            return repeat(maskChar, value.length());
        }

        return value.substring(0, visibleStart)
                + repeat(maskChar, value.length() - visibleStart - visibleEnd)
                + value.substring(value.length() - visibleEnd);
    }

    private String repeat(String value, int times) {
        return value.repeat(Math.max(0, times));
    }
}
