package pe.edu.galaxy.training.java.gt.creditcard.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreditCardResponse(
        Long id,
        String cardHolderName,
        String cardNumber,
        String cvv,
        Integer expirationMonth,
        Integer expirationYear,
        BigDecimal creditLimit,
        BigDecimal availableBalance,
        String status,
        LocalDateTime createdAt) {
}
