package pe.edu.galaxy.training.java.ms.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import pe.edu.galaxy.training.java.sensitive.annotation.Mask;
import pe.edu.galaxy.training.java.sensitive.annotation.Sensitive;
import pe.edu.galaxy.training.java.sensitive.enums.MaskType;
import pe.edu.galaxy.training.java.sensitive.enums.SensitivityLevel;

import java.math.BigDecimal;

@Schema(description = "Customer creation or update request")
public record CustomerRequest(

        @Schema(description = "Customer name", example = "Juan Perez Luna")
        @NotBlank(message = "Name is required")
        String name,

        @Schema(description = "Customer address", example = "Av. Lima 780 - Miraflores")
        @NotNull(message = "Address is required")
        @Sensitive(level = SensitivityLevel.CRITICAL)
        @Mask(type = MaskType.FULL)
        String address,

        @Schema(description = "Customer phone", example = "+51 95050 0689")
        @NotNull(message = "Phone is required")
        @Mask(type = MaskType.PHONE,visibleStart = 3,maskChar = "-",visibleEnd = 2)
        @Sensitive(level = SensitivityLevel.CRITICAL)
        String phone,

        @Mask(type = MaskType.EMAIL)
        @Sensitive(level = SensitivityLevel.HIGH)

        @Schema(description = "Customer email", example = "demo@dominio.com")

        @NotNull(message = "email is required")
        @Email(message = "Customer email format invalid")
        String email
) {
}