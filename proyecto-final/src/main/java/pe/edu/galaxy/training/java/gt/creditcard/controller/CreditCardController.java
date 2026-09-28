package pe.edu.galaxy.training.java.gt.creditcard.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pe.edu.galaxy.training.java.gt.creditcard.dto.CardStatusUpdateRequest;
import pe.edu.galaxy.training.java.gt.creditcard.dto.CreditCardResponse;
import pe.edu.galaxy.training.java.gt.creditcard.dto.IssueCardRequest;
import pe.edu.galaxy.training.java.gt.creditcard.dto.TransactionRequest;
import pe.edu.galaxy.training.java.gt.creditcard.dto.TransactionResponse;
import pe.edu.galaxy.training.java.gt.creditcard.service.CreditCardService;

/**
 * Implementa el contrato contracts/openapi-creditcard.yaml. Los metodos devuelven objetos de
 * dominio simples: AndesResponseBodyAdvice (andes-api-server-spring-boot-starter) los envuelve
 * automaticamente en el envelope ApiResponse, y GlobalExceptionHandler traduce cualquier
 * AndesApiException (lanzada por CreditCardServiceImpl) al codigo HTTP correspondiente.
 */
@RestController
@RequestMapping("/api/v1/credit-cards")
@RequiredArgsConstructor
public class CreditCardController {

    private final CreditCardService creditCardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreditCardResponse issueCard(@Valid @RequestBody IssueCardRequest request) {
        return creditCardService.issueCard(request);
    }

    @GetMapping("/{cardId}")
    public CreditCardResponse findById(@PathVariable Long cardId) {
        return creditCardService.findById(cardId);
    }

    @PatchMapping("/{cardId}/status")
    public CreditCardResponse updateStatus(@PathVariable Long cardId,
                                            @Valid @RequestBody CardStatusUpdateRequest request) {
        return creditCardService.updateStatus(cardId, request);
    }

    @PostMapping("/{cardId}/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse authorizeTransaction(@PathVariable Long cardId,
                                                     @Valid @RequestBody TransactionRequest request) {
        return creditCardService.authorizeTransaction(cardId, request);
    }

    @GetMapping("/{cardId}/transactions")
    public List<TransactionResponse> listTransactions(@PathVariable Long cardId) {
        return creditCardService.listTransactions(cardId);
    }
}
