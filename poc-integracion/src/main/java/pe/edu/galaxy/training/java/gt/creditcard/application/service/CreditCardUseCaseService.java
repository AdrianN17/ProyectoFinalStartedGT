package pe.edu.galaxy.training.java.gt.creditcard.application.service;

import static pe.edu.galaxy.training.java.gt.creditcard.commons.CreditCardConstants.BUSINESS_KEY_CARD;
import static pe.edu.galaxy.training.java.gt.creditcard.commons.CreditCardConstants.BUSINESS_KEY_TRANSACTION;
import static pe.edu.galaxy.training.java.gt.creditcard.commons.CreditCardConstants.FRAUD_REJECT_THRESHOLD;
import static pe.edu.galaxy.training.java.gt.creditcard.commons.CreditCardConstants.STATUS_ACTIVE;
import static pe.edu.galaxy.training.java.gt.creditcard.commons.CreditCardConstants.STATUS_CANCELLED;
import static pe.edu.galaxy.training.java.gt.creditcard.commons.CreditCardConstants.TX_APPROVED;
import static pe.edu.galaxy.training.java.gt.creditcard.commons.CreditCardConstants.TX_REJECTED;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.andes.api.common.exception.AndesConflictException;
import pe.andes.api.common.exception.AndesNotFoundException;
import pe.andes.api.common.exception.AndesValidationException;
import pe.andes.lib.id.ChecksumUtils;
import pe.andes.lib.id.autoconfigure.IdGeneratorService;
import pe.andes.lib.text.MaskUtils;
import pe.andes.lib.text.SlugUtils;
import pe.edu.galaxy.training.java.audit.annotation.Auditable;
import pe.edu.galaxy.training.java.gt.creditcard.application.port.in.CreditCardUseCase;
import pe.edu.galaxy.training.java.gt.creditcard.application.port.out.CreditCardPersistencePort;
import pe.edu.galaxy.training.java.gt.creditcard.application.port.out.FraudEvaluationPort;
import pe.edu.galaxy.training.java.gt.creditcard.dto.CardStatusUpdateRequest;
import pe.edu.galaxy.training.java.gt.creditcard.dto.CreditCardResponse;
import pe.edu.galaxy.training.java.gt.creditcard.dto.IssueCardRequest;
import pe.edu.galaxy.training.java.gt.creditcard.dto.TransactionRequest;
import pe.edu.galaxy.training.java.gt.creditcard.dto.TransactionResponse;
import pe.edu.galaxy.training.java.gt.creditcard.entity.CreditCardEntity;
import pe.edu.galaxy.training.java.gt.creditcard.entity.CreditCardTransactionEntity;
import pe.edu.galaxy.training.java.gt.creditcard.fraud.generated.FraudCheckRequest;
import pe.edu.galaxy.training.java.gt.creditcard.fraud.generated.FraudCheckResponse;
import pe.edu.galaxy.training.java.gt.creditcard.mapper.CreditCardMapper;
import pe.edu.galaxy.training.java.gt.creditcard.mapper.TransactionMapper;
import pe.edu.galaxy.training.java.logs.annotation.LogOperation;
import pe.edu.galaxy.training.java.observability.annotation.ObservedMetric;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditCardUseCaseService implements CreditCardUseCase {

    private final CreditCardPersistencePort persistencePort;
    private final FraudEvaluationPort fraudEvaluationPort;
    private final IdGeneratorService idGeneratorService;

    @ObservedMetric(name = "creditcard.issue", description = "Credit card issuance", operation = "CARD_ISSUE")
    @Auditable(operation = "CREATE", entity = BUSINESS_KEY_CARD, description = "Issue credit card")
    @LogOperation(value = "CARD_ISSUE", businessKey = BUSINESS_KEY_CARD)
    @Override
    public CreditCardResponse issueCard(IssueCardRequest request) {
        CreditCardEntity entity = CreditCardMapper.toEntity(request);
        return CreditCardMapper.toResponse(persistencePort.saveCard(entity));
    }

    @Auditable(operation = "FIND_BY_ID", entity = BUSINESS_KEY_CARD, description = "Find credit card by id")
    @Override
    public CreditCardResponse findById(Long cardId) {
        return CreditCardMapper.toResponse(findCardOrThrow(cardId));
    }

    @ObservedMetric(name = "creditcard.updateStatus", description = "Credit card status update", operation = "CARD_UPDATE_STATUS")
    @Auditable(operation = "UPDATE_STATUS", entity = BUSINESS_KEY_CARD, description = "Credit card status update")
    @LogOperation(value = "CARD_UPDATE_STATUS", businessKey = BUSINESS_KEY_CARD)
    @Override
    public CreditCardResponse updateStatus(Long cardId, CardStatusUpdateRequest request) {
        CreditCardEntity entity = findCardOrThrow(cardId);
        if (STATUS_CANCELLED.equals(entity.getStatus())) {
            throw new AndesConflictException("Credit card is already CANCELLED: " + cardId);
        }
        entity.setStatus(request.status());
        return CreditCardMapper.toResponse(persistencePort.saveCard(entity));
    }

    @ObservedMetric(name = "creditcard.transaction.authorize", description = "Transaction authorization", operation = "TRANSACTION_AUTHORIZE")
    @Auditable(operation = "AUTHORIZE", entity = BUSINESS_KEY_TRANSACTION, description = "Authorize credit card transaction")
    @LogOperation(value = "TRANSACTION_AUTHORIZE", businessKey = BUSINESS_KEY_TRANSACTION)
    @Override
    public TransactionResponse authorizeTransaction(Long cardId, TransactionRequest request) {
        CreditCardEntity card = findCardOrThrow(cardId);

        if (!STATUS_ACTIVE.equals(card.getStatus())) {
            throw new AndesConflictException("Credit card is not ACTIVE: " + cardId);
        }
        if ("PURCHASE".equals(request.type()) && request.amount().compareTo(card.getAvailableBalance()) > 0) {
            throw new AndesValidationException("Insufficient credit limit for card: " + cardId);
        }

        FraudCheckResponse fraudCheck = fraudEvaluationPort.evaluate(
                new FraudCheckRequest(cardId, request.merchant(), request.amount().doubleValue()));

        String referenceId = idGeneratorService.newId("TXN");
        String merchantSlug = SlugUtils.slugify(request.merchant());
        String idempotencyKey = ChecksumUtils.sha256Hex(
                cardId + "|" + request.merchant() + "|" + request.amount() + "|" + referenceId);
        log.info(
                "Autorizando transaccion {} para tarjeta {} (idempotencyKey={})",
                referenceId,
                MaskUtils.maskDigits(card.getCardNumber(), 4),
                idempotencyKey);

        CreditCardTransactionEntity transaction = new CreditCardTransactionEntity();
        transaction.setReferenceId(referenceId);
        transaction.setCardId(cardId);
        transaction.setMerchant(request.merchant());
        transaction.setMerchantSlug(merchantSlug);
        transaction.setAmount(request.amount());
        transaction.setType(request.type());
        transaction.setFraudScore(fraudCheck.getRiskScore());

        if (fraudCheck.getRiskScore() >= FRAUD_REJECT_THRESHOLD) {
            transaction.setStatus(TX_REJECTED);
        } else {
            transaction.setStatus(TX_APPROVED);
            card.applyTransaction(request.amount(), request.type());
            persistencePort.saveCard(card);
        }

        return TransactionMapper.toResponse(persistencePort.saveTransaction(transaction));
    }

    @Override
    public List<TransactionResponse> listTransactions(Long cardId) {
        findCardOrThrow(cardId);
        return persistencePort.findTransactionsByCardId(cardId).stream()
                .map(TransactionMapper::toResponse)
                .toList();
    }

    private CreditCardEntity findCardOrThrow(Long cardId) {
        return persistencePort.findCardById(cardId)
                .orElseThrow(() -> new AndesNotFoundException("Credit card not found with id: " + cardId));
    }
}
