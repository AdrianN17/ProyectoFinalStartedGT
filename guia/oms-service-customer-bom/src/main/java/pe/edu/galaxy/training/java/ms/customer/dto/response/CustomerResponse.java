package pe.edu.galaxy.training.java.ms.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.edu.galaxy.training.java.sensitive.annotation.Mask;
import pe.edu.galaxy.training.java.sensitive.annotation.Sensitive;
import pe.edu.galaxy.training.java.sensitive.enums.MaskType;
import pe.edu.galaxy.training.java.sensitive.enums.SensitivityLevel;

import java.math.BigDecimal;

@Schema(description = "Customer response data")
public record CustomerResponse(

        @Schema(description = "Customer unique identifier", example = "1")
        Long id,

        @Schema(description = "Customer name", example = "Juan Perez Luna")
        String name,

        @Mask(type = MaskType.PARTIAL,visibleStart = 5,visibleEnd = 1,maskChar = "#")
        @Schema(description = "Customer address", example = "Av. Lima 780 - Miraflores")
        String address,


        @Schema(description = "Customer phone", example = "+51 95050 0689")
        @Mask(type = MaskType.PHONE,visibleStart = 3,maskChar = "*",visibleEnd = 2)
        //@Sensitive(level = SensitivityLevel.CRITICAL)
        String phone,

        @Schema(description = "Customer email", example = "demo@dominio.com")
        @Mask(type = MaskType.EMAIL)
        //@Sensitive(level = SensitivityLevel.HIGH)
        String email
) {
}