package pe.edu.galaxy.training.java.logs.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import pe.edu.galaxy.training.java.logs.annotation.LogOperation;
import pe.edu.galaxy.training.java.logs.util.LogConstants;

@Slf4j
@Aspect
public class OperationLoggingAspect {
    @Around("@annotation(logOperation)")
    public Object logOperation(ProceedingJoinPoint joinPoint, LogOperation logOperation) throws Throwable {
        long start = System.currentTimeMillis();
        MDC.put(LogConstants.OPERATION, logOperation.value());
        MDC.put(LogConstants.BUSINESS_KEY, logOperation.businessKey());

        try {
            log.info("BUSINESS_OPERATION_START operation={} class={} method={} businessKey={}",
                    logOperation.value(),
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    logOperation.businessKey());

            Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();
            log.info("BUSINESS_OPERATION_SUCCESS operation={} elapsedMs={}",logOperation.value(), end- start);
            return result;
        } catch (Throwable ex) {
            log.error("BUSINESS_OPERATION_ERROR operation={} elapsedMs={} error={}",
                    logOperation.value(), System.currentTimeMillis() - start, ex.getMessage(), ex);
            throw ex;
        } finally {
            MDC.remove(LogConstants.OPERATION);
            MDC.remove(LogConstants.BUSINESS_KEY);
        }
    }
}
