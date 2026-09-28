package pe.andes.poc.server.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pe.andes.poc.server.generated.model.CustomerRequest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerControllerIT {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void createAndFetchCustomerRoundTrip() {
        CustomerRequest request = new CustomerRequest().fullName("Ada Lovelace").email("ada@example.com");

        ResponseEntity<String> createResponse =
                restTemplate.postForEntity(url("/api/v1/customers"), request, String.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).contains("\"success\":true").contains("ada@example.com");
    }

    @Test
    void notFoundCustomerReturnsStandardErrorEnvelope() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/api/v1/customers/999999"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("\"success\":false").contains("NOT_FOUND");
    }

    @Test
    void invalidCustomerReturnsValidationError() {
        CustomerRequest invalid = new CustomerRequest().fullName("ab").email("not-an-email");
        ResponseEntity<String> response = restTemplate.postForEntity(url("/api/v1/customers"), invalid, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
