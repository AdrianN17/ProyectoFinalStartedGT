package pe.edu.galaxy.training.java.gt.creditcard.mapper;

import pe.edu.galaxy.training.java.gt.creditcard.dto.CreditCardResponse;
import pe.edu.galaxy.training.java.gt.creditcard.dto.IssueCardRequest;
import pe.edu.galaxy.training.java.gt.creditcard.entity.CreditCardEntity;

public final class CreditCardMapper {

    private CreditCardMapper() {
    }

    public static CreditCardEntity toEntity(IssueCardRequest request) {
        CreditCardEntity entity = new CreditCardEntity();
        entity.setCardHolderName(request.cardHolderName());
        entity.setCardNumber(request.cardNumber());
        entity.setCvv(request.cvv());
        entity.setExpirationMonth(request.expirationMonth());
        entity.setExpirationYear(request.expirationYear());
        entity.setCreditLimit(request.creditLimit());
        entity.setAvailableBalance(request.creditLimit());
        return entity;
    }

    public static CreditCardResponse toResponse(CreditCardEntity entity) {
        return new CreditCardResponse(
                entity.getId(),
                entity.getCardHolderName(),
                entity.getCardNumber(),
                entity.getCvv(),
                entity.getExpirationMonth(),
                entity.getExpirationYear(),
                entity.getCreditLimit(),
                entity.getAvailableBalance(),
                entity.getStatus(),
                entity.getCreatedAt());
    }
}
