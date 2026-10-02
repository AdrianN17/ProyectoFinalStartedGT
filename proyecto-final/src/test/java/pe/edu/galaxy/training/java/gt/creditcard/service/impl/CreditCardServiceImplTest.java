package pe.edu.galaxy.training.java.gt.creditcard.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.andes.api.common.exception.AndesConflictException;
import pe.andes.api.common.exception.AndesNotFoundException;
import pe.andes.api.common.exception.AndesValidationException;
import pe.andes.lib.id.autoconfigure.IdGeneratorService;
import pe.edu.galaxy.training.java.gt.creditcard.dto.IssueCardRequest;
import pe.edu.galaxy.training.java.gt.creditcard.dto.TransactionRequest;
import pe.edu.galaxy.training.java.gt.creditcard.entity.CreditCardEntity;
import pe.edu.galaxy.training.java.gt.creditcard.entity.CreditCardTransactionEntity;
import pe.edu.galaxy.training.java.gt.creditcard.fraud.FraudCheckClient;
import pe.edu.galaxy.training.java.gt.creditcard.fraud.generated.FraudCheckResponse;
import pe.edu.galaxy.training.java.gt.creditcard.repository.CreditCardRepository;
import pe.edu.galaxy.training.java.gt.creditcard.repository.CreditCardTransactionRepository;

@ExtendWith(MockitoExtension.class)
class CreditCardServiceImplTest {

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private CreditCardTransactionRepository transactionRepository;

    @Mock
    private FraudCheckClient fraudCheckClient;

    @Mock
    private IdGeneratorService idGeneratorService;

    @InjectMocks
    private CreditCardServiceImpl creditCardService;

    private CreditCardEntity card;

    @BeforeEach
    void setUp() {
        card = new CreditCardEntity();
        card.setId(1L);
        card.setCardHolderName("Ana Lopez");
        card.setCardNumber("4111111111111111");
        card.setCvv("123");
        card.setCreditLimit(BigDecimal.valueOf(5000));
        card.setAvailableBalance(BigDecimal.valueOf(5000));
        card.setStatus("ACTIVE");
    }

    @Test
    void issueCardPersistsWithAvailableBalanceEqualToCreditLimit() {
        IssueCardRequest request = new IssueCardRequest(
                "Ana Lopez", "4111111111111111", "123", 12, 2030, BigDecimal.valueOf(5000));
        when(creditCardRepository.save(any(CreditCardEntity.class))).thenReturn(card);

        var response = creditCardService.issueCard(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.availableBalance()).isEqualByComparingTo(BigDecimal.valueOf(5000));
    }

    @Test
    void findByIdThrowsNotFoundWhenMissing() {
        when(creditCardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> creditCardService.findById(99L))
                .isInstanceOf(AndesNotFoundException.class);
    }

    @Test
    void authorizeTransactionRejectsWhenCardIsNotActive() {
        card.setStatus("BLOCKED");
        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(card));

        TransactionRequest request = new TransactionRequest("Amazon", BigDecimal.valueOf(100), "PURCHASE");

        assertThatThrownBy(() -> creditCardService.authorizeTransaction(1L, request))
                .isInstanceOf(AndesConflictException.class);
    }

    @Test
    void authorizeTransactionRejectsWhenAmountExceedsAvailableBalance() {
        card.setAvailableBalance(BigDecimal.valueOf(50));
        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(card));

        TransactionRequest request = new TransactionRequest("Amazon", BigDecimal.valueOf(100), "PURCHASE");

        assertThatThrownBy(() -> creditCardService.authorizeTransaction(1L, request))
                .isInstanceOf(AndesValidationException.class);
    }

    @Test
    void authorizeTransactionApprovesAndDebitsBalanceWhenFraudScoreIsLow() {
        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(fraudCheckClient.evaluate(any())).thenReturn(new FraudCheckResponse(10));
        when(idGeneratorService.newId("TXN")).thenReturn("TXN-01TEST000000000000000001");
        when(transactionRepository.save(any(CreditCardTransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest request = new TransactionRequest("Amazon", BigDecimal.valueOf(200), "PURCHASE");
        var response = creditCardService.authorizeTransaction(1L, request);

        assertThat(response.status()).isEqualTo("APPROVED");
        assertThat(response.referenceId()).isEqualTo("TXN-01TEST000000000000000001");
        assertThat(response.merchantSlug()).isEqualTo("amazon");
        assertThat(card.getAvailableBalance()).isEqualByComparingTo(BigDecimal.valueOf(4800));
    }

    @Test
    void authorizeTransactionRejectsWhenFraudScoreIsHighAndDoesNotDebitBalance() {
        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(fraudCheckClient.evaluate(any())).thenReturn(new FraudCheckResponse(95));
        when(idGeneratorService.newId("TXN")).thenReturn("TXN-01TEST000000000000000002");
        when(transactionRepository.save(any(CreditCardTransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest request = new TransactionRequest("Suspicious Shop", BigDecimal.valueOf(4000), "PURCHASE");
        var response = creditCardService.authorizeTransaction(1L, request);

        assertThat(response.status()).isEqualTo("REJECTED");
        assertThat(response.merchantSlug()).isEqualTo("suspicious-shop");
        assertThat(card.getAvailableBalance()).isEqualByComparingTo(BigDecimal.valueOf(5000));
    }
}
