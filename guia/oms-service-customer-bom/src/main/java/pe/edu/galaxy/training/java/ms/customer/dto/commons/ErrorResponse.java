package pe.edu.galaxy.training.java.ms.customer.dto.commons;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}