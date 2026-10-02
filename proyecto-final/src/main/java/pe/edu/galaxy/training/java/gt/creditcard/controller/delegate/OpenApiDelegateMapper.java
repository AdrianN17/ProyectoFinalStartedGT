package pe.edu.galaxy.training.java.gt.creditcard.controller.delegate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import pe.edu.galaxy.training.java.gt.creditcard.dto.CardStatusUpdateRequest;
import pe.edu.galaxy.training.java.gt.creditcard.dto.CreditCardResponse;
import pe.edu.galaxy.training.java.gt.creditcard.dto.IssueCardRequest;
import pe.edu.galaxy.training.java.gt.creditcard.dto.TransactionResponse;
import pe.edu.galaxy.training.java.gt.creditcard.generated.server.model.CreditCard;
import pe.edu.galaxy.training.java.gt.creditcard.generated.server.model.CreditCardEnvelope;
import pe.edu.galaxy.training.java.gt.creditcard.generated.server.model.Transaction;
import pe.edu.galaxy.training.java.gt.creditcard.generated.server.model.TransactionEnvelope;
import pe.edu.galaxy.training.java.gt.creditcard.generated.server.model.TransactionListEnvelope;

final class OpenApiDelegateMapper {

    private OpenApiDelegateMapper() {
    }

    static IssueCardRequest toServiceIssueCardRequest(
            pe.edu.galaxy.training.java.gt.creditcard.generated.server.model.IssueCardRequest request) {
        return new IssueCardRequest(
                request.getCardHolderName(),
                request.getCardNumber(),
                request.getCvv(),
                request.getExpirationMonth(),
                request.getExpirationYear(),
                request.getCreditLimit());
    }

    static CardStatusUpdateRequest toServiceCardStatusUpdateRequest(
            pe.edu.galaxy.training.java.gt.creditcard.generated.server.model.CardStatusUpdateRequest request) {
        return new CardStatusUpdateRequest(request.getStatus().getValue());
    }

    static pe.edu.galaxy.training.java.gt.creditcard.dto.TransactionRequest toServiceTransactionRequest(
            pe.edu.galaxy.training.java.gt.creditcard.generated.server.model.TransactionRequest request) {
        return new pe.edu.galaxy.training.java.gt.creditcard.dto.TransactionRequest(
                request.getMerchant(),
                request.getAmount(),
                request.getType().getValue());
    }

    static CreditCardEnvelope toCreditCardEnvelope(CreditCardResponse response) {
        return new CreditCardEnvelope()
                .success(true)
                .data(toCreditCard(response));
    }

    static TransactionEnvelope toTransactionEnvelope(TransactionResponse response) {
        return new TransactionEnvelope()
                .success(true)
                .data(toTransaction(response));
    }

    static TransactionListEnvelope toTransactionListEnvelope(List<TransactionResponse> responses) {
        return new TransactionListEnvelope()
                .success(true)
                .data(responses.stream().map(OpenApiDelegateMapper::toTransaction).toList());
    }

    private static CreditCard toCreditCard(CreditCardResponse response) {
        return new CreditCard()
                .id(response.id())
                .cardHolderName(response.cardHolderName())
                .cardNumber(response.cardNumber())
                .cvv(response.cvv())
                .expirationMonth(response.expirationMonth())
                .expirationYear(response.expirationYear())
                .creditLimit(response.creditLimit())
                .availableBalance(response.availableBalance())
                .status(response.status() == null ? null : CreditCard.StatusEnum.fromValue(response.status()))
                .createdAt(toOffsetDateTime(response.createdAt()));
    }

    private static Transaction toTransaction(TransactionResponse response) {
        return new Transaction()
                .id(response.id())
                .cardId(response.cardId())
                .merchant(response.merchant())
                .amount(response.amount())
                .type(response.type() == null ? null : Transaction.TypeEnum.fromValue(response.type()))
                .status(response.status() == null ? null : Transaction.StatusEnum.fromValue(response.status()))
                .fraudScore(response.fraudScore())
                .createdAt(toOffsetDateTime(response.createdAt()));
    }

    private static OffsetDateTime toOffsetDateTime(java.time.LocalDateTime value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}
