package pe.edu.galaxy.training.java.gt.creditcard.controller.delegate;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pe.edu.galaxy.training.java.gt.creditcard.generated.server.api.TransactionsApiDelegate;
import pe.edu.galaxy.training.java.gt.creditcard.generated.server.model.TransactionEnvelope;
import pe.edu.galaxy.training.java.gt.creditcard.generated.server.model.TransactionListEnvelope;
import pe.edu.galaxy.training.java.gt.creditcard.generated.server.model.TransactionRequest;
import pe.edu.galaxy.training.java.gt.creditcard.service.CreditCardService;

@Service
@RequiredArgsConstructor
public class TransactionsApiDelegateImpl implements TransactionsApiDelegate {

    private final CreditCardService creditCardService;

    @Override
    public ResponseEntity<TransactionEnvelope> authorizeTransaction(Long cardId, TransactionRequest request) {
        var response = creditCardService.authorizeTransaction(cardId, OpenApiDelegateMapper.toServiceTransactionRequest(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(OpenApiDelegateMapper.toTransactionEnvelope(response));
    }

    @Override
    public ResponseEntity<TransactionListEnvelope> listTransactions(Long cardId) {
        var responses = creditCardService.listTransactions(cardId);
        return ResponseEntity.ok(OpenApiDelegateMapper.toTransactionListEnvelope(responses));
    }
}
