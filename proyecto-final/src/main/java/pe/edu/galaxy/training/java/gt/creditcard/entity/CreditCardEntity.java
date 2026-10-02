package pe.edu.galaxy.training.java.gt.creditcard.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.andes.api.common.exception.AndesValidationException;
import pe.edu.galaxy.training.java.gt.creditcard.commons.CreditCardConstants;
import pe.edu.galaxy.training.java.sensitive.annotation.Encrypt;
import pe.edu.galaxy.training.java.sensitive.annotation.Mask;
import pe.edu.galaxy.training.java.sensitive.annotation.Sensitive;
import pe.edu.galaxy.training.java.sensitive.enums.MaskType;
import pe.edu.galaxy.training.java.sensitive.enums.SensitivityLevel;

/**
 * El numero de tarjeta y el CVV se cifran en reposo ({@code @Encrypt}, oms-starter-security-core)
 * y se enmascaran en las respuestas JSON ({@code @Mask}); el acceso a ambos campos queda marcado
 * como PCI de alta sensibilidad ({@code @Sensitive}).
 */
@Entity
@Table(name = "credit_card")
@Getter
@Setter
@NoArgsConstructor
public class CreditCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardHolderName;

    @Encrypt
    @Mask(type = MaskType.CARD)
    @Sensitive(level = SensitivityLevel.HIGH, category = "PCI")
    @Column(length = 1024)
    private String cardNumber;

    @Encrypt
    @Mask(type = MaskType.FULL)
    @Sensitive(level = SensitivityLevel.HIGH, category = "PCI")
    @Column(length = 1024)
    private String cvv;

    private Integer expirationMonth;

    private Integer expirationYear;

    private BigDecimal creditLimit;

    private BigDecimal availableBalance;

    private String status;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = CreditCardConstants.STATUS_ACTIVE;
        }
        if (this.availableBalance == null) {
            this.availableBalance = this.creditLimit;
        }
    }

    public void applyTransaction(BigDecimal amount, String transactionType) {
        BigDecimal delta = switch (transactionType) {
            case "PURCHASE" -> amount.negate();
            case "PAYMENT", "REFUND" -> amount;
            default -> throw new AndesValidationException("Unsupported transaction type: " + transactionType);
        };
        this.availableBalance = this.availableBalance.add(delta);
    }
}
