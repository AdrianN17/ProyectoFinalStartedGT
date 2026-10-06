package pe.edu.galaxy.training.java.gt.creditcard.application.port.in;

import java.util.List;

import pe.edu.galaxy.training.java.gt.creditcard.dto.CardStatusUpdateRequest;
import pe.edu.galaxy.training.java.gt.creditcard.dto.CreditCardResponse;
import pe.edu.galaxy.training.java.gt.creditcard.dto.IssueCardRequest;
import pe.edu.galaxy.training.java.gt.creditcard.dto.TransactionRequest;
import pe.edu.galaxy.training.java.gt.creditcard.dto.TransactionResponse;

public interface CreditCardUseCase {

    CreditCardResponse issueCard(IssueCardRequest request);

    CreditCardResponse findById(Long cardId);

    CreditCardResponse updateStatus(Long cardId, CardStatusUpdateRequest request);

    TransactionResponse authorizeTransaction(Long cardId, TransactionRequest request);

    List<TransactionResponse> listTransactions(Long cardId);
}
