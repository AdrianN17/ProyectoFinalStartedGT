package pe.edu.galaxy.training.java.audit.message;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class AuditLogMessage {

    private String serviceName;

    private String operation;

    private String entityName;

    private String description;

    private String status;

    private Long executionTimeMs;

    private String requestPayload;

    private String responsePayload;

    private String errorMessage;

    private LocalDateTime createdAt;
}
