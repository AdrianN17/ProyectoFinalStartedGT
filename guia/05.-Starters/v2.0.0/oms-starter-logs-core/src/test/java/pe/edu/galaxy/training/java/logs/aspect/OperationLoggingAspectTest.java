package pe.edu.galaxy.training.java.logs.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import pe.edu.galaxy.training.java.logs.annotation.LogOperation;
import pe.edu.galaxy.training.java.logs.util.LogConstants;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OperationLoggingAspectTest {

    private final OperationLoggingAspect aspect = new OperationLoggingAspect();

    @Test
    void logOperationReturnsResultAndClearsMdcOnSuccess() throws Throwable {
        LogOperation logOperation = annotationFor("operationOk");
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(signature.getDeclaringTypeName()).thenReturn("SomeClass");
        when(signature.getName()).thenReturn("someMethod");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.proceed()).thenReturn("result");

        Object result = aspect.logOperation(joinPoint, logOperation);

        assertThat(result).isEqualTo("result");
        verify(joinPoint).proceed();
        assertThat(MDC.get(LogConstants.OPERATION)).isNull();
        assertThat(MDC.get(LogConstants.BUSINESS_KEY)).isNull();
    }

    @Test
    void logOperationPropagatesExceptionAndClearsMdc() throws Throwable {
        LogOperation logOperation = annotationFor("operationOk");
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(signature.getDeclaringTypeName()).thenReturn("SomeClass");
        when(signature.getName()).thenReturn("someMethod");
        when(joinPoint.getSignature()).thenReturn(signature);
        RuntimeException failure = new RuntimeException("boom");
        when(joinPoint.proceed()).thenThrow(failure);

        assertThatThrownBy(() -> aspect.logOperation(joinPoint, logOperation))
                .isSameAs(failure);

        assertThat(MDC.get(LogConstants.OPERATION)).isNull();
        assertThat(MDC.get(LogConstants.BUSINESS_KEY)).isNull();
    }

    private LogOperation annotationFor(String methodName) throws NoSuchMethodException {
        Method method = AnnotatedSample.class.getDeclaredMethod(methodName);
        for (Annotation annotation : method.getAnnotations()) {
            if (annotation instanceof LogOperation logOperation) {
                return logOperation;
            }
        }
        throw new IllegalStateException("LogOperation annotation not found on " + methodName);
    }

    private static class AnnotatedSample {
        @LogOperation(value = "CREATE_CARD", businessKey = "card-123")
        void operationOk() {
        }
    }
}
