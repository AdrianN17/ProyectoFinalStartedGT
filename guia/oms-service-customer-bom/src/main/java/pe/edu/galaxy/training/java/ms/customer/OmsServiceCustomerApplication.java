package pe.edu.galaxy.training.java.ms.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/*
@EntityScan(basePackages = {
        "pe.edu.galaxy.training.java.ms.customer.entity",
        "pe.edu.galaxy.training.java.audit.entity"
})
@EnableJpaRepositories(basePackages = {
        "pe.edu.galaxy.training.java.ms.customer.repository",
        "pe.edu.galaxy.training.java.audit.repository"
})*/

@ConfigurationPropertiesScan
@EnableJpaAuditing
@SpringBootApplication
public class OmsServiceCustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OmsServiceCustomerApplication.class, args);
    }
}