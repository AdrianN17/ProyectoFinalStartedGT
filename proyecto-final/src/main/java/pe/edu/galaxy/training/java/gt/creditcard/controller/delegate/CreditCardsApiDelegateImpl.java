package pe.edu.galaxy.training.java.gt.creditcard.controller.delegate;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pe.edu.galaxy.training.java.gt.creditcard.generated.server.api.CreditCardsApiDelegate;
import pe.edu.galaxy.training.java.gt.creditcard.generated.server.model.CardStatusUpdateRequest;
import pe.edu.galaxy.training.java.gt.creditcard.generated.server.model.CreditCardEnvelope;
import pe.edu.galaxy.training.java.gt.creditcard.generated.server.model.IssueCardRequest;
import pe.edu.galaxy.training.java.gt.creditcard.service.CreditCardService;

@Service
@RequiredArgsConstructor
public class CreditCardsApiDelegateImpl implements CreditCardsApiDelegate {

    private final CreditCardService creditCardService;

    @Override
    public ResponseEntity<CreditCardEnvelope> issueCreditCard(IssueCardRequest request) {
        var response = creditCardService.issueCard(OpenApiDelegateMapper.toServiceIssueCardRequest(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(OpenApiDelegateMapper.toCreditCardEnvelope(response));
    }

    @Override
    public ResponseEntity<CreditCardEnvelope> getCreditCard(Long cardId) {
        var response = creditCardService.findById(cardId);
        return ResponseEntity.ok(OpenApiDelegateMapper.toCreditCardEnvelope(response));
    }

    @Override
    public ResponseEntity<CreditCardEnvelope> updateCreditCardStatus(Long cardId, CardStatusUpdateRequest request) {
        var response = creditCardService.updateStatus(cardId, OpenApiDelegateMapper.toServiceCardStatusUpdateRequest(request));
        return ResponseEntity.ok(OpenApiDelegateMapper.toCreditCardEnvelope(response));
    }
}
