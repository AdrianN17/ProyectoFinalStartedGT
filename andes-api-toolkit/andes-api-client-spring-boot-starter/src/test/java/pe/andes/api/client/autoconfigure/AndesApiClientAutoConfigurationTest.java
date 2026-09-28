package pe.andes.api.client.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import pe.andes.api.client.AndesApiClientRegistry;

import static org.assertj.core.api.Assertions.assertThat;

class AndesApiClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AndesApiClientAutoConfiguration.class));

    @Test
    void createsOneClientPerConfiguredEntry() {
        contextRunner
                .withPropertyValues(
                        "andes.api.client.clients.customer.base-url=http://localhost:8081",
                        "andes.api.client.clients.payment.base-url=http://localhost:8082")
                .run(context -> {
                    assertThat(context).hasSingleBean(AndesApiClientRegistry.class);
                    AndesApiClientRegistry registry = context.getBean(AndesApiClientRegistry.class);
                    assertThat(registry.getAll()).containsKeys("customer", "payment");
                });
    }

    @Test
    void registryIsEmptyWhenNoClientsConfigured() {
        contextRunner.run(context -> {
            AndesApiClientRegistry registry = context.getBean(AndesApiClientRegistry.class);
            assertThat(registry.getAll()).isEmpty();
        });
    }
}
