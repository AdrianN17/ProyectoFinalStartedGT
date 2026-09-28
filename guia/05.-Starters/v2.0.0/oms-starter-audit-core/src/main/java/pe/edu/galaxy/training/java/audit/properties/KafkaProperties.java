package pe.edu.galaxy.training.java.audit.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
public class KafkaProperties {
    private String bootstrapServers;
    private String topicName;
}
