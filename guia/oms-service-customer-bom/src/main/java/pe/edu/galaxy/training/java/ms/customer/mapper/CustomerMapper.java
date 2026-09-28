package pe.edu.galaxy.training.java.ms.customer.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.galaxy.training.java.ms.customer.dto.request.CustomerRequest;
import pe.edu.galaxy.training.java.ms.customer.dto.response.CustomerResponse;
import pe.edu.galaxy.training.java.ms.customer.entity.CustomerEntity;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    CustomerEntity toEntity(CustomerRequest request);

    CustomerResponse toResponse(CustomerEntity entity);

}

