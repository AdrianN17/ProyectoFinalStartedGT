package pe.edu.galaxy.training.java.gt.creditcard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CardStatusUpdateRequest(
        @NotBlank @Pattern(regexp = "ACTIVE|BLOCKED|CANCELLED") String status) {
}
