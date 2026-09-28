package pe.edu.galaxy.training.java.ms.customer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.galaxy.training.java.ms.customer.dto.request.CustomerRequest;
import pe.edu.galaxy.training.java.ms.customer.dto.response.CustomerResponse;
import pe.edu.galaxy.training.java.ms.customer.service.CustomerService;

@Tag(name = "Customers", description = "Customer management REST APIs")
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class    CustomerController {

    private final CustomerService customerService;


    @Operation(
            summary = "Get paginated customers",
            description = "Returns customers using pagination and sorting parameters."
    )
    @GetMapping
    public Page<CustomerResponse> findAll(
            @ParameterObject Pageable pageable
    ) {
        return customerService.findAll(pageable);
    }

    @Operation(
            summary = "Get customer by ID",
            description = "Returns a single customer using its unique identifier."
    )
    @GetMapping("/{id}")
    public CustomerResponse findById(
            @Parameter(
                    description = "Customer unique identifier",
                    example = "1",
                    required = true
            )
            @Positive(message = "The customer id must be greater than zero") @PathVariable Long id
    ) {
        return customerService.findById(id);
    }

    @Operation(
            summary = "Create customer",
            description = "Creates a new customer with name, address and phone information."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(
            @Parameter(
                    description = "Customer data required to create a new customer",
                    required = true
            )
            @Valid @RequestBody CustomerRequest request
    ) {
        customerService.create(request);
    }

    @Operation(
            summary = "Update customer",
            description = "Updates an existing customer using its ID and request body data."
    )
    @PutMapping("/{id}")
    public void update(
            @Parameter(
                    description = "Customer unique identifier",
                    example = "1",
                    required = true
            )
            @PathVariable Long id,

            @Parameter(
                    description = "Customer data required to update an existing customer",
                    required = true
            )
            @Valid @RequestBody CustomerRequest request
    ) {
        customerService.update(id, request);
    }

    @Operation(
            summary = "Delete customer",
            description = "Deletes an existing customer using its unique identifier."
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(
                    description = "Customer unique identifier",
                    example = "1",
                    required = true
            )
            @PathVariable Long id
    ) {
        customerService.delete(id);
    }
}