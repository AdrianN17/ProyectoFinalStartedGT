package pe.edu.galaxy.training.java.gt.creditcard.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "oms.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditServiceBusQueueConfig {

    @Bean("auditServiceBusSenderClient")
    ServiceBusSenderClient auditServiceBusSenderClient(
            org.springframework.core.env.Environment environment) {

        String connectionString = environment.getProperty("oms.audit.service-bus.connection-string");
        String queueName = environment.getProperty("oms.audit.service-bus.queue-name");

        if (connectionString == null || connectionString.isBlank()) {
            throw new IllegalStateException("oms.audit.service-bus.connection-string es obligatorio.");
        }
        if (queueName == null || queueName.isBlank()) {
            throw new IllegalStateException("oms.audit.service-bus.queue-name es obligatorio en Service Bus Basic.");
        }

        return new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .queueName(queueName)
                .buildClient();
    }
}
