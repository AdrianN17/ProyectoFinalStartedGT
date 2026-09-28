package pe.andes.api.common.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Describes a single validation or business error attached to an {@link ApiError}.
 */
public final class ApiErrorDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String field;
    private final String code;
    private final String message;
    private final Object rejectedValue;

    public ApiErrorDetail(String field, String code, String message, Object rejectedValue) {
        this.field = field;
        this.code = code;
        this.message = message;
        this.rejectedValue = rejectedValue;
    }

    public static ApiErrorDetail of(String field, String message) {
        return new ApiErrorDetail(field, null, message, null);
    }

    public static ApiErrorDetail of(String field, String code, String message) {
        return new ApiErrorDetail(field, code, message, null);
    }

    public String getField() {
        return field;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApiErrorDetail that)) {
            return false;
        }
        return Objects.equals(field, that.field)
                && Objects.equals(code, that.code)
                && Objects.equals(message, that.message)
                && Objects.equals(rejectedValue, that.rejectedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, code, message, rejectedValue);
    }

    @Override
    public String toString() {
        return "ApiErrorDetail{" +
                "field='" + field + '\'' +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", rejectedValue=" + rejectedValue +
                '}';
    }
}
