package pe.edu.galaxy.training.java.audit.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pe.edu.galaxy.training.java.audit.annotation.Auditable;
import pe.edu.galaxy.training.java.audit.dto.AuditEvent;
import pe.edu.galaxy.training.java.audit.service.AuditService;

@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "oms.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditAspect {

    private final AuditService auditService;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            auditService.save(AuditEvent.builder()
                    .operation(auditable.operation())
                    .entityName(auditable.entity())
                    .description(auditable.description())
                    .status("SUCCESS")
                    .executionTimeMs(System.currentTimeMillis() - start)
                    .request(joinPoint.getArgs())
                    .response(result)
                    .build());
            return result;
        } catch (Throwable ex) {
            auditService.save(AuditEvent.builder()
                    .operation(auditable.operation())
                    .entityName(auditable.entity())
                    .description(auditable.description())
                    .status("ERROR")
                    .executionTimeMs(System.currentTimeMillis() - start)
                    .request(joinPoint.getArgs())
                    .errorMessage(ex.getMessage())
                    .build());
            throw ex;
        }
    }
}
