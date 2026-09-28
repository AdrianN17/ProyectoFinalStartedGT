package pe.andes.api.server.autoconfigure;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import pe.andes.api.server.config.AndesServerProperties;
import pe.andes.api.server.error.AndesExceptionMapper;
import pe.andes.api.server.error.GlobalExceptionHandler;
import pe.andes.api.server.openapi.AndesOpenApiFactory;
import pe.andes.api.server.web.AndesResponseBodyAdvice;
import pe.andes.api.server.web.CorrelationIdFilter;

import java.util.List;

/**
 * Auto-registers Andes API Server components: correlation id filter, centralized error
 * handling, response wrapping and an OpenAPI metadata bean. Every feature can be disabled
 * via {@code andes.api.server.*} properties, and every bean backs off if the application
 * already defines its own ({@code @ConditionalOnMissingBean}).
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(AndesServerProperties.class)
public class AndesApiServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "andes.api.server.correlation", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<CorrelationIdFilter> andesCorrelationIdFilter(AndesServerProperties properties) {
        CorrelationIdFilter filter = new CorrelationIdFilter(properties.getCorrelation().isGenerateIfMissing());
        FilterRegistrationBean<CorrelationIdFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "andes.api.server.error-handling", name = "enabled", havingValue = "true", matchIfMissing = true)
    public GlobalExceptionHandler andesGlobalExceptionHandler(List<AndesExceptionMapper<?>> exceptionMappers,
                                                                AndesServerProperties properties) {
        return new GlobalExceptionHandler(exceptionMappers, properties.getErrorHandling().isIncludeStackTrace());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "andes.api.server.response", name = "wrap-enabled", havingValue = "true", matchIfMissing = true)
    public AndesResponseBodyAdvice andesResponseBodyAdvice() {
        return new AndesResponseBodyAdvice();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(OpenAPI.class)
    @ConditionalOnProperty(prefix = "andes.api.server.openapi", name = "enabled", havingValue = "true", matchIfMissing = true)
    public OpenAPI andesOpenApi(AndesServerProperties properties) {
        return AndesOpenApiFactory.build(properties.getOpenapi());
    }
}
