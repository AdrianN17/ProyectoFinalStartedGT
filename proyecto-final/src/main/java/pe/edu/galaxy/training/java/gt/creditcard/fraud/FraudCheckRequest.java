package pe.edu.galaxy.training.java.gt.creditcard.fraud;

import java.math.BigDecimal;

public record FraudCheckRequest(Long cardId, String merchant, BigDecimal amount) {
}
