package pe.edu.galaxy.training.java.audit.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import pe.edu.galaxy.training.java.audit.properties.AuditProperties;
import pe.edu.galaxy.training.java.audit.properties.ServiceBusProperties;

/**
 * Autoconfiguracion del cliente de envio de eventos de auditoria hacia un
 * cola de Azure Service Bus, en reemplazo del {@code KafkaTemplate} usado
 * previamente con Apache Kafka.
 *
 * <p>Service Bus se autentica siempre con connection string (clave compartida
 * del namespace/cola); no se usa {@code DefaultAzureCredential} aqui para
 * mantener el flujo de credenciales (az login) reservado exclusivamente a
 * Azure Key Vault en {@code oms-starter-security-core}.</p>
 *
 * <p>A diferencia de {@code KafkaTemplate} (conexion lazy), el SDK de Azure
 * Service Bus valida la conexion al construir el {@code ServiceBusSenderClient}
 * ({@code buildClient()}), por lo que este autoconfig se activa unicamente
 * cuando {@code oms.audit.enabled=true} (igual que {@code AuditAspect}), para
 * no romper arranques en entornos donde la auditoria esta deshabilitada.</p>
 */
@AutoConfiguration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "oms.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ServiceBusAutoConfig {

	private final AuditProperties properties;

	@Bean("auditServiceBusSenderClient")
	@ConditionalOnMissingBean(name = "auditServiceBusSenderClient")
	ServiceBusSenderClient auditServiceBusSenderClient() {

		ServiceBusProperties serviceBus = properties.getServiceBus();

		if (serviceBus.getConnectionString() == null || serviceBus.getConnectionString().isBlank()) {
			throw new IllegalStateException(
					"oms.audit.service-bus.connection-string es obligatorio: Service Bus se autentica "
							+ "con connection string, no con credenciales por defecto (az login).");
		}
		if (serviceBus.getQueueName() == null || serviceBus.getQueueName().isBlank()) {
			throw new IllegalStateException("oms.audit.service-bus.queue-name es obligatorio.");
		}

		return new ServiceBusClientBuilder()
				.connectionString(serviceBus.getConnectionString())
				.sender()
				.queueName(serviceBus.getQueueName())
				.buildClient();
	}
}
