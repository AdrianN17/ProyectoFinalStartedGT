package pe.edu.galaxy.training.java.example;

import pe.edu.galaxy.training.java.observability.annotation.ObservedMetric;
import pe.edu.galaxy.training.java.observability.service.BusinessMetricsService;

public class CustomerService {

    private final BusinessMetricsService metricsService;

    public CustomerService(BusinessMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @ObservedMetric(
            name = "oms.customer.search",
            description = "Customer search operation",
            operation = "search-customers"
    )
    public void searchCustomers() {
        metricsService.incrementCreated("customer");
    }
}
