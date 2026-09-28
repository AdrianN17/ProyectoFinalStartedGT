package pe.andes.poc.client.orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class OrdersDemoControllerIT {

    private static final String BASE_URL = "http://localhost:8082";

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void fetchesExistingOrderThroughAndesApiClient() {
        ResponseEntity<String> response =
                restTemplate.getForEntity(BASE_URL + "/api/v1/orders-demo/ORD-1", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true").contains("ORD-1");
    }

    @Test
    void missingOrderIsMappedToNotFound() {
        ResponseEntity<String> response =
                restTemplate.getForEntity(BASE_URL + "/api/v1/orders-demo/DOES-NOT-EXIST", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("\"success\":false");
    }
}
