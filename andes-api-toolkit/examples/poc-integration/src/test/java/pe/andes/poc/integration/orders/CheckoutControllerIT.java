package pe.andes.poc.integration.orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class CheckoutControllerIT {

    private static final String BASE_URL = "http://localhost:8083";

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void checkoutFlowsThroughOwnApiBusinessLogicAndClientLibrary() {
        Map<String, Object> request = Map.of(
                "customerId", 1,
                "items", java.util.List.of(Map.of("sku", "SKU-1", "quantity", 2, "unitPrice", 10.0)));

        ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL + "/api/v1/checkouts", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("\"success\":true").contains("PENDING");
    }

    @Test
    void notFoundOrderIsMappedThroughClientAndServerErrorHandling() {
        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL + "/api/v1/checkouts/UNKNOWN", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("\"success\":false");
    }
}
