package pe.andes.api.server.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.assertj.AssertableWebApplicationContext;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import pe.andes.api.server.error.GlobalExceptionHandler;
import pe.andes.api.server.web.AndesResponseBodyAdvice;

import static org.assertj.core.api.Assertions.assertThat;

class AndesApiServerAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AndesApiServerAutoConfiguration.class));

    @Test
    void registersDefaultBeans() {
        contextRunner.run((AssertableWebApplicationContext context) -> {
            assertThat(context).hasSingleBean(GlobalExceptionHandler.class);
            assertThat(context).hasSingleBean(AndesResponseBodyAdvice.class);
            assertThat(context).getBean("andesCorrelationIdFilter").isNotNull();
        });
    }

    @Test
    void disablesErrorHandlingWhenPropertySetToFalse() {
        contextRunner.withPropertyValues("andes.api.server.error-handling.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(GlobalExceptionHandler.class));
    }

    @Test
    void backsOffWhenUserProvidesOwnBean() {
        contextRunner.withUserConfiguration(CustomHandlerConfig.class)
                .run(context -> assertThat(context).getBean(GlobalExceptionHandler.class)
                        .isSameAs(context.getBean(CustomHandlerConfig.class).customHandler));
    }

    static class CustomHandlerConfig {
        final GlobalExceptionHandler customHandler = new GlobalExceptionHandler(java.util.List.of(), true);

        @org.springframework.context.annotation.Bean
        GlobalExceptionHandler globalExceptionHandler() {
            return customHandler;
        }
    }
}
