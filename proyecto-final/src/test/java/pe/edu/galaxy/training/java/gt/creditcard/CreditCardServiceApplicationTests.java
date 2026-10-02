package pe.edu.galaxy.training.java.gt.creditcard;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import pe.edu.galaxy.training.java.gt.creditcard.config.KeyVaultTestConfig;

/**
 * Prueba end-to-end con el contexto completo (Galaxy Starters + andes-api-toolkit activos):
 * emite una tarjeta, valida el enmascarado del numero/CVV, autoriza una compra de bajo riesgo
 * (aprobada, contra el simulador interno de fraude via andes-api-client) y una de monto alto
 * (rechazada por el simulador), y valida el mapeo de un 404 de andes-api-server.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(KeyVaultTestConfig.class)
class CreditCardServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void issueCardMasksCardNumberAndCvvInResponse() throws Exception {
        mockMvc.perform(post("/api/v1/credit-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cardHolderName":"Ana Lopez","cardNumber":"4111111111111111",
                                 "cvv":"123","expirationMonth":12,"expirationYear":2030,"creditLimit":5000}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cardNumber").value(not("4111111111111111")))
                .andExpect(jsonPath("$.data.cvv").value(not("123")))
                .andExpect(jsonPath("$.data.availableBalance").value(5000));
    }

    @Test
    void lowAmountTransactionIsApprovedThroughFraudCheckSimulator() throws Exception {
        Long cardId = issueCard();

        mockMvc.perform(post("/api/v1/credit-cards/" + cardId + "/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"merchant":"Amazon","amount":200,"type":"PURCHASE"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    void highAmountTransactionIsRejectedThroughFraudCheckSimulator() throws Exception {
        Long cardId = issueCard();

        mockMvc.perform(post("/api/v1/credit-cards/" + cardId + "/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"merchant":"Suspicious Shop","amount":4000,"type":"PURCHASE"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    void blockingCancelledCardIsConflict() throws Exception {
        Long cardId = issueCard();

        mockMvc.perform(patch("/api/v1/credit-cards/" + cardId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/credit-cards/" + cardId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BLOCKED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void missingCardIsMappedToNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/credit-cards/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    private Long issueCard() throws Exception {
        String body = mockMvc.perform(post("/api/v1/credit-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cardHolderName":"Ana Lopez","cardNumber":"4111111111111111",
                                 "cvv":"123","expirationMonth":12,"expirationYear":2030,"creditLimit":5000}
                                """))
                .andReturn().getResponse().getContentAsString();

        com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(body);
        return node.get("data").get("id").asLong();
    }
}
