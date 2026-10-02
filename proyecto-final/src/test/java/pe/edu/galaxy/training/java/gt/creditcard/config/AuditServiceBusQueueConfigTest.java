package pe.edu.galaxy.training.java.gt.creditcard.config;

import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditServiceBusQueueConfigTest {

    private final AuditServiceBusQueueConfig config = new AuditServiceBusQueueConfig();

    @Test
    void throwsWhenConnectionStringIsMissing() {
        MockEnvironment env = new MockEnvironment()
                .withProperty("oms.audit.service-bus.queue-name", "queue-audit");

        assertThatThrownBy(() -> config.auditServiceBusSenderClient(env))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("connection-string");
    }

    @Test
    void throwsWhenQueueNameIsMissing() {
        MockEnvironment env = new MockEnvironment()
                .withProperty(
                        "oms.audit.service-bus.connection-string",
                        "Endpoint=sb://example.servicebus.windows.net/;SharedAccessKeyName=test;SharedAccessKey=test");

        assertThatThrownBy(() -> config.auditServiceBusSenderClient(env))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("queue-name");
    }

    @Test
    void createsQueueSenderClientWhenPropertiesArePresent() {
        MockEnvironment env = new MockEnvironment()
                .withProperty(
                        "oms.audit.service-bus.connection-string",
                        "Endpoint=sb://example.servicebus.windows.net/;SharedAccessKeyName=test;SharedAccessKey=test")
                .withProperty("oms.audit.service-bus.queue-name", "queue-audit");

        ServiceBusSenderClient client = config.auditServiceBusSenderClient(env);
        assertThat(client).isNotNull();
        client.close();
    }
}
