package pe.edu.galaxy.training.java.ms.customer.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.edu.galaxy.training.java.ms.customer.dto.request.CustomerRequest;
import pe.edu.galaxy.training.java.ms.customer.dto.response.CustomerResponse;

public interface CustomerService {

    Page<CustomerResponse> findAll(Pageable pageable);

    CustomerResponse findById(Long id);

    Long create(CustomerRequest request);

    void update(Long id, CustomerRequest request);

    void delete(Long id);
}