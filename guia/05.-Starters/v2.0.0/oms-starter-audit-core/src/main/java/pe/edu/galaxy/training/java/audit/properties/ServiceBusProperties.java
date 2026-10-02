package pe.edu.galaxy.training.java.audit.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceBusProperties {

    /**
     * Connection string completo del namespace de Azure Service Bus
     * (clave compartida, Shared Access Signature). Es obligatorio: Service Bus
     * no se autentica con {@code DefaultAzureCredential}/az login en este starter.
     */
    private String connectionString;

    /**
     * Nombre del topico de Service Bus donde se publican los eventos de auditoria.
     */
    private String topicName;
}
