package pe.edu.galaxy.training.java.gt.creditcard.mapper;

import pe.edu.galaxy.training.java.gt.creditcard.dto.TransactionResponse;
import pe.edu.galaxy.training.java.gt.creditcard.entity.CreditCardTransactionEntity;

public final class TransactionMapper {

    private TransactionMapper() {
    }

    public static TransactionResponse toResponse(CreditCardTransactionEntity entity) {
        return new TransactionResponse(
                entity.getId(),
                entity.getReferenceId(),
                entity.getCardId(),
                entity.getMerchant(),
                entity.getMerchantSlug(),
                entity.getAmount(),
                entity.getType(),
                entity.getStatus(),
                entity.getFraudScore(),
                entity.getCreatedAt());
    }
}
