package pe.edu.galaxy.training.java.sensitive.processor;

import pe.edu.galaxy.training.java.sensitive.annotation.Encrypt;
import pe.edu.galaxy.training.java.sensitive.annotation.Mask;
import pe.edu.galaxy.training.java.sensitive.annotation.Sensitive;
import pe.edu.galaxy.training.java.sensitive.enums.EncryptionProvider;
import pe.edu.galaxy.training.java.sensitive.policy.SensitivePolicy;
import pe.edu.galaxy.training.java.sensitive.properties.SensitiveSecurityProperties;
import pe.edu.galaxy.training.java.sensitive.service.EncryptService;
import pe.edu.galaxy.training.java.sensitive.service.SensitiveAuditService;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.Temporal;
import java.util.*;

public class SensitiveFieldProcessor {

    private final EncryptService encryptService;
    private final SensitiveAuditService auditService;
    private final SensitiveSecurityProperties properties;
    private final SensitivePolicy sensitivePolicy;
    public SensitiveFieldProcessor(EncryptService encryptService,
                                   SensitiveAuditService auditService,
                                   SensitiveSecurityProperties properties,
                                   SensitivePolicy sensitivePolicy) {
        this.encryptService = encryptService;
        this.auditService = auditService;
        this.properties = properties;
        this.sensitivePolicy = sensitivePolicy;
    }

    public void encryptObject(Object target) {
        process(target, true);
    }

    public void decryptObject(Object target) {
        process(target, false);
    }

    public void decryptResult(Object result) {
        if (result == null) {
            return;
        }

        if (result instanceof Optional<?> optional) {
            optional.ifPresent(this::decryptObject);
            return;
        }

        if (result instanceof Iterable<?> iterable) {
            iterable.forEach(this::decryptObject);
            return;
        }

        decryptObject(result);
    }

    private void process(Object target, boolean encryptMode) {
        if (target == null || !properties.getEncrypt().isEnabled()) {
            return;
        }

        for (Field field : target.getClass().getDeclaredFields()) {
            Encrypt encrypt = field.getAnnotation(Encrypt.class);
            Sensitive sensitive = field.getAnnotation(Sensitive.class);

            if (sensitive != null && properties.getAudit().isEnabled() && !encryptMode) {
                auditService.fieldAccessed(target.getClass().getName(), field.getName(), sensitive);
            }

            if (encrypt == null || !String.class.equals(field.getType())) {
                continue;
            }

            try {
                field.setAccessible(true);
                String value = (String) field.get(target);

                if (value == null || value.isBlank()) {
                    continue;
                }

                EncryptionProvider provider = encrypt.provider();

                if (EncryptionProvider.NONE.equals(provider)) {
                    continue;
                }

                if (!encryptService.supports(provider)) {
                    throw new IllegalStateException("Unsupported encryption provider: " + provider);
                }

                String newValue = encryptMode
                        ? encryptService.encrypt(value)
                        : encryptService.decrypt(value);

                field.set(target, newValue);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Error processing field: " + field.getName(), ex);
            }
        }
    }

    public Object sanitizeForAudit(Object target) {

        if (target == null) {
            return null;
        }

        Class<?> type = target.getClass();

        if (isSimpleType(type)) {
            return target;
        }

        if (target instanceof Iterable<?> iterable) {
            List<Object> sanitizedList = new ArrayList<>();

            for (Object item : iterable) {
                sanitizedList.add(sanitizeForAudit(item));
            }

            return sanitizedList;
        }

        if (target instanceof Map<?, ?> map) {
            Map<Object, Object> sanitizedMap = new LinkedHashMap<>();

            map.forEach((key, value) ->
                    sanitizedMap.put(key, sanitizeForAudit(value)));

            return sanitizedMap;
        }

        if (!isAuditCandidate(type)) {
            return target;
        }

        Map<String, Object> payload = new LinkedHashMap<>();

        for (Field field : type.getDeclaredFields()) {
            try {
                field.setAccessible(true);

                Object value = field.get(target);

                Sensitive sensitive = field.getAnnotation(Sensitive.class);
                Mask mask = field.getAnnotation(Mask.class);

                if (sensitive != null && properties.getAudit().isEnabled()) {
                    Object sanitized = sensitivePolicy.apply(
                            value,
                            sensitive.level(),
                            mask
                    );

                    payload.put(field.getName(), sanitized);
                } else {
                    payload.put(field.getName(), sanitizeForAudit(value));
                }

            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(
                        "Error sanitizing field: " + field.getName(), ex);
            }
        }

        return payload;
    }
    private boolean isSimpleType(Class<?> type) {

        return type.isPrimitive()
                || String.class.equals(type)
                || Boolean.class.equals(type)
                || Character.class.equals(type)
                || Number.class.isAssignableFrom(type)
                || BigDecimal.class.equals(type)
                || BigInteger.class.equals(type)
                || UUID.class.equals(type)
                || Date.class.isAssignableFrom(type)
                || Temporal.class.isAssignableFrom(type)
                || Enum.class.isAssignableFrom(type);
    }

    private boolean isAuditCandidate(Class<?> type) {

        String packageName = type.getPackageName();

        return !packageName.startsWith("java.")
                && !packageName.startsWith("javax.")
                && !packageName.startsWith("jakarta.")
                && !packageName.startsWith("org.springframework.")
                && !packageName.startsWith("com.fasterxml.");
    }
}
