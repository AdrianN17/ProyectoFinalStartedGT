package pe.edu.galaxy.training.java.gt.creditcard.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record TransactionRequest(
        @NotBlank String merchant,
        @NotNull @Positive BigDecimal amount,
        @NotBlank @Pattern(regexp = "PURCHASE|PAYMENT|REFUND") String type) {
}
