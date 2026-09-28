package pe.edu.galaxy.training.java.sensitive.service;

import pe.edu.galaxy.training.java.sensitive.annotation.Sensitive;

public interface SensitiveAuditService {

    void fieldAccessed(String className, String fieldName, Sensitive sensitive);
}
