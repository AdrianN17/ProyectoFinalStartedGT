package pe.andes.poc.server.customer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.MDC;
import pe.andes.api.common.http.AndesApiConstants;
import pe.andes.api.common.model.ApiMetadata;
import pe.andes.api.common.model.PageResponse;
import pe.andes.poc.server.generated.api.CustomersApiDelegate;
import pe.andes.poc.server.generated.model.Customer;
import pe.andes.poc.server.generated.model.CustomerEnvelope;
import pe.andes.poc.server.generated.model.CustomerPage;
import pe.andes.poc.server.generated.model.CustomerPageEnvelope;
import pe.andes.poc.server.generated.model.CustomerRequest;

/**
 * Business-logic implementation of the {@code CustomersApi} contract generated from
 * {@code contracts/openapi-server.yaml}. The generated {@code CustomersApiController}
 * delegates every request here; this is the only hand-written piece of the API surface.
 */
@Service
public class CustomerApiDelegateImpl implements CustomersApiDelegate {

    private final CustomerService customerService;

    public CustomerApiDelegateImpl(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public ResponseEntity<CustomerPageEnvelope> listCustomers(Integer page, Integer size) {
        int resolvedPage = page != null ? page : 0;
        int resolvedSize = size != null ? size : 20;
        PageResponse<Customer> result = customerService.list(resolvedPage, resolvedSize);

        CustomerPage data = new CustomerPage()
                .content(result.getContent())
                .pagination(result.getPagination());
        CustomerPageEnvelope envelope = new CustomerPageEnvelope()
                .success(true)
                .data(data)
                .metadata(currentMetadata());
        return ResponseEntity.ok(envelope);
    }

    @Override
    public ResponseEntity<CustomerEnvelope> createCustomer(CustomerRequest customerRequest) {
        Customer created = customerService.create(customerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(successEnvelope(created));
    }

    @Override
    public ResponseEntity<CustomerEnvelope> getCustomerById(Long id) {
        return ResponseEntity.ok(successEnvelope(customerService.getById(id)));
    }

    @Override
    public ResponseEntity<Void> deleteCustomer(Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private CustomerEnvelope successEnvelope(Customer customer) {
        return new CustomerEnvelope().success(true).data(customer).metadata(currentMetadata());
    }

    private ApiMetadata currentMetadata() {
        String correlationId = MDC.get(AndesApiConstants.MDC_CORRELATION_ID);
        return ApiMetadata.builder()
                .traceId(correlationId)
                .correlationId(correlationId)
                .requestId(MDC.get(AndesApiConstants.MDC_REQUEST_ID))
                .build();
    }
}
