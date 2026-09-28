package pe.edu.galaxy.training.java.observability.service;

public interface BusinessMetricsService {

    void incrementCreated(String entity);

    void incrementUpdated(String entity);

    void incrementDeleted(String entity);

    void incrementError(String entity, String operation);

    void recordExecutionTime(String operation, long milliseconds);
}
