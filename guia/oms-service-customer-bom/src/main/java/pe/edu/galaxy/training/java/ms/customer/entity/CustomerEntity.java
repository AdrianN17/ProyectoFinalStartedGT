package pe.edu.galaxy.training.java.ms.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.edu.galaxy.training.java.sensitive.annotation.Encrypt;
import pe.edu.galaxy.training.java.sensitive.annotation.Mask;
import pe.edu.galaxy.training.java.sensitive.annotation.Sensitive;
import pe.edu.galaxy.training.java.sensitive.enums.MaskType;
import pe.edu.galaxy.training.java.sensitive.enums.SensitivityLevel;

@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Encrypt
    private String address;

    private String phone;

    @Encrypt
    //@Mask(type = MaskType.EMAIL)
    //@Sensitive(level = SensitivityLevel.HIGH)
    private String email;
}

