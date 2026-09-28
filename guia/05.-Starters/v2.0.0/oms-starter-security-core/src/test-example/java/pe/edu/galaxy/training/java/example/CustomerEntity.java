package pe.edu.galaxy.training.java.example;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import pe.edu.galaxy.training.java.sensitive.annotation.Encrypt;
import pe.edu.galaxy.training.java.sensitive.annotation.Mask;
import pe.edu.galaxy.training.java.sensitive.annotation.Sensitive;
import pe.edu.galaxy.training.java.sensitive.enums.MaskType;
import pe.edu.galaxy.training.java.sensitive.enums.SensitivityLevel;

@Entity
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Encrypt
    @Mask(type = MaskType.EMAIL)
    @Sensitive(level = SensitivityLevel.HIGH, category = "PII")
    private String email;

    @Encrypt
    @Mask(type = MaskType.PHONE)
    @Sensitive(level = SensitivityLevel.HIGH, category = "PII")
    private String phone;

    @Encrypt
    @Mask(type = MaskType.DOCUMENT)
    @Sensitive(level = SensitivityLevel.CRITICAL, category = "DOCUMENT")
    private String documentNumber;
}
