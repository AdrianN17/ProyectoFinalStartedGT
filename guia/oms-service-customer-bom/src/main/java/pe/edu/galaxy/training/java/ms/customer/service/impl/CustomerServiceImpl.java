package pe.edu.galaxy.training.java.ms.customer.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pe.edu.galaxy.training.java.audit.annotation.Auditable;
import pe.edu.galaxy.training.java.ms.customer.dto.request.CustomerRequest;
import pe.edu.galaxy.training.java.ms.customer.dto.response.CustomerResponse;
import pe.edu.galaxy.training.java.ms.customer.entity.CustomerEntity;
import pe.edu.galaxy.training.java.ms.customer.exception.ResourceNotFoundException;
import pe.edu.galaxy.training.java.ms.customer.mapper.CustomerMapper;
import pe.edu.galaxy.training.java.ms.customer.repository.CustomerRepository;
import pe.edu.galaxy.training.java.ms.customer.service.CustomerService;

import pe.edu.galaxy.training.java.logs.annotation.LogOperation;
import pe.edu.galaxy.training.java.observability.annotation.ObservedMetric;

import static pe.edu.galaxy.training.java.ms.customer.commons.GlobalConstants.BUSINESS_KEY;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    //@Auditable(operation = "FIND_ALL", entity = BUSINESS_KEY, description = "Find all customers")
    @Override
    public Page<CustomerResponse> findAll(Pageable pageable) {
        return customerRepository.findAllByState("1",pageable)
                .map(customerMapper::toResponse);
    }

    @Auditable(operation = "FIND_BY_ID", entity = BUSINESS_KEY, description = "Find by customer ID")
    @Override
    public CustomerResponse findById(Long id) {
        CustomerEntity product = customerRepository.findById(id)
                .orElseThrow(() -> handlerNotFoundException(id));
        return customerMapper.toResponse(product);
    }

    @ObservedMetric(name ="create",description = "Customer create operation", operation = "CUSTOMER_CREATE")
    @Auditable(operation = "CREATE", entity = BUSINESS_KEY, description = "Customer create ")
    @LogOperation(value = "CUSTOMER_CREATE", businessKey = BUSINESS_KEY)
    @Override
    public Long create(CustomerRequest request) {
        CustomerEntity product = customerMapper.toEntity(request);
        CustomerEntity resCustomerEntity = customerRepository.save(product);
        return resCustomerEntity.getId();
    }

    @ObservedMetric(name ="update",description = "Customer update operation", operation = "CUSTOMER_UPDATE")
    @Auditable(operation = "UPDATE", entity = BUSINESS_KEY, description = "Customer update")
    @LogOperation(value = "CUSTOMER_UPDATE", businessKey = BUSINESS_KEY)
    @Override
    public void update(Long id, CustomerRequest request) {
        CustomerEntity product = customerRepository.findById(id)
                .filter(p->p.getState().equals("1"))
                .orElseThrow(() -> handlerNotFoundException(id));

        product.setName(request.name());

        product.setAddress(request.address());

        product.setPhone(request.phone());

        customerRepository.save(product);

    }

    @ObservedMetric(name ="delete",description = "Customer delete operation", operation = "CUSTOMER_DELETE")
    @Auditable(operation = "DELETE", entity = BUSINESS_KEY, description = "Customer delete")
    @LogOperation(value = "CUSTOMER_DELETE", businessKey = BUSINESS_KEY)
    @Override
    public void delete(Long id) {

        CustomerEntity product = customerRepository.findById(id)
                .orElseThrow(() -> handlerNotFoundException(id));

        product.setState("0");

        customerRepository.save(product);
    }

    private RuntimeException handlerNotFoundException(Long id){
        return new ResourceNotFoundException("Customer not found with id: " + id);
    }
}