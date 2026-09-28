package pe.edu.galaxy.training.java.audit.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuditEvent {
    private String operation;
    private String entityName;
    private String description;
    private String status;
    private Long executionTimeMs;
    private Object request;
    private Object response;
    private String errorMessage;
}
