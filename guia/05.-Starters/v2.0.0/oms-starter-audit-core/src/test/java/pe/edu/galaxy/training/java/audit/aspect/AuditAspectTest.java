package pe.edu.galaxy.training.java.audit.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pe.edu.galaxy.training.java.audit.annotation.Auditable;
import pe.edu.galaxy.training.java.audit.dto.AuditEvent;
import pe.edu.galaxy.training.java.audit.service.AuditService;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditAspectTest {

    private final AuditService auditService = mock(AuditService.class);
    private final AuditAspect aspect = new AuditAspect(auditService);

    @Test
    void auditSavesSuccessEventAndReturnsResult() throws Throwable {
        Auditable auditable = annotationFor("createCard");
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn("created");
        when(joinPoint.getArgs()).thenReturn(new Object[] {"arg1"});

        Object result = aspect.audit(joinPoint, auditable);

        assertThat(result).isEqualTo("created");
        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditService).save(captor.capture());
        AuditEvent event = captor.getValue();
        assertThat(event.getOperation()).isEqualTo("CREATE");
        assertThat(event.getEntityName()).isEqualTo("CreditCard");
        assertThat(event.getStatus()).isEqualTo("SUCCESS");
        assertThat(event.getResponse()).isEqualTo("created");
        assertThat(event.getErrorMessage()).isNull();
    }

    @Test
    void auditSavesErrorEventAndRethrowsException() throws Throwable {
        Auditable auditable = annotationFor("createCard");
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        RuntimeException failure = new RuntimeException("insufficient funds");
        when(joinPoint.proceed()).thenThrow(failure);
        when(joinPoint.getArgs()).thenReturn(new Object[] {"arg1"});

        assertThatThrownBy(() -> aspect.audit(joinPoint, auditable)).isSameAs(failure);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditService).save(captor.capture());
        AuditEvent event = captor.getValue();
        assertThat(event.getStatus()).isEqualTo("ERROR");
        assertThat(event.getErrorMessage()).isEqualTo("insufficient funds");
        assertThat(event.getResponse()).isNull();
    }

    private Auditable annotationFor(String methodName) throws NoSuchMethodException {
        Method method = AnnotatedSample.class.getDeclaredMethod(methodName);
        for (Annotation annotation : method.getAnnotations()) {
            if (annotation instanceof Auditable auditable) {
                return auditable;
            }
        }
        throw new IllegalStateException("Auditable annotation not found on " + methodName);
    }

    private static class AnnotatedSample {
        @Auditable(operation = "CREATE", entity = "CreditCard", description = "Creates a credit card")
        void createCard() {
        }
    }
}
