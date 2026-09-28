package pe.edu.galaxy.training.java.gt.creditcard.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long cardId,
        String merchant,
        BigDecimal amount,
        String type,
        String status,
        Integer fraudScore,
        LocalDateTime createdAt) {
}
