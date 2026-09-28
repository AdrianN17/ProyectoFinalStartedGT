package pe.edu.galaxy.training.java.audit.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "oms.audit")
public class AuditProperties {
    private boolean enabled = true;
    private String serviceName = "unknown-service";
    private boolean logRequest = true;
    private boolean logResponse = true;
    private boolean logErrors = true;
    private int maxPayloadLength = 8_000;
    private KafkaProperties kafka;

}
