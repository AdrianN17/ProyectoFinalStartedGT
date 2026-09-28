package pe.edu.galaxy.training.java.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", length = 120, nullable = false)
    private String serviceName;

    @Column(name = "operation", length = 80, nullable = false)
    private String operation;

    @Column(name = "entity_name", length = 120, nullable = false)
    private String entityName;

    @Column(name = "description", length = 250)
    private String description;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "request_payload", columnDefinition = "TEXT")
    private String requestPayload;

    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
