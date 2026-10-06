package pe.edu.galaxy.training.java.gt.creditcard.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "credit_card_transaction")
@Getter
@Setter
@NoArgsConstructor
public class CreditCardTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cardId;

    /**
     * Identificador externo tipo ULID con prefijo "TXN" (andes-id-generator-spring-boot-starter,
     * bean {@code IdGeneratorService}), ordenable por tiempo de creacion - util como referencia
     * de transaccion para el cliente, distinto del id autoincremental interno.
     */
    private String referenceId;

    private String merchant;

    /**
     * Slug normalizado del comercio (andes-text-utils, {@code SlugUtils.slugify}), usado para
     * agrupar/correlacionar transacciones del mismo comercio sin depender de mayusculas, acentos
     * o espacios en el nombre libre capturado en {@code merchant}.
     */
    private String merchantSlug;

    private BigDecimal amount;

    private String type;

    private String status;

    private Integer fraudScore;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
