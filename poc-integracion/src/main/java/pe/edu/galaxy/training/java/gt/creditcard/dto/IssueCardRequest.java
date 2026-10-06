package pe.edu.galaxy.training.java.gt.creditcard.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record IssueCardRequest(
        @NotBlank String cardHolderName,
        @NotBlank @Pattern(regexp = "\\d{16}", message = "cardNumber must have 16 digits") String cardNumber,
        @NotBlank @Pattern(regexp = "\\d{3,4}", message = "cvv must have 3 or 4 digits") String cvv,
        @NotNull Integer expirationMonth,
        @NotNull Integer expirationYear,
        @NotNull @Positive BigDecimal creditLimit) {
}
