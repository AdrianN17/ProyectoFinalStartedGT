package pe.andes.poc.server.customer;

import org.springframework.stereotype.Service;
import pe.andes.api.common.exception.AndesConflictException;
import pe.andes.api.common.exception.AndesNotFoundException;
import pe.andes.api.common.model.PageResponse;
import pe.andes.poc.server.generated.model.Customer;
import pe.andes.poc.server.generated.model.CustomerRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory Customer repository/service. {@code Customer}/{@code CustomerRequest} are
 * generated from {@code contracts/openapi-server.yaml} (API-first): this class only
 * implements business logic, never redefines the resource shape by hand.
 */
@Service
public class CustomerService {

    private final Map<Long, Customer> customers = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    public PageResponse<Customer> list(int page, int size) {
        List<Customer> all = customers.values().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .toList();
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        return PageResponse.of(all.subList(from, to), page, size, all.size());
    }

    public Customer getById(Long id) {
        Customer customer = customers.get(id);
        if (customer == null) {
            throw new AndesNotFoundException("Customer " + id + " not found");
        }
        return customer;
    }

    public Customer create(CustomerRequest request) {
        boolean emailTaken = customers.values().stream()
                .anyMatch(c -> c.getEmail().equalsIgnoreCase(request.getEmail()));
        if (emailTaken) {
            throw new AndesConflictException("Email already registered: " + request.getEmail());
        }
        long id = idSequence.incrementAndGet();
        Customer customer = new Customer()
                .id(id)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .createdAt(OffsetDateTime.now());
        customers.put(id, customer);
        return customer;
    }

    public void delete(Long id) {
        if (customers.remove(id) == null) {
            throw new AndesNotFoundException("Customer " + id + " not found");
        }
    }
}

