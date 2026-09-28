package pe.edu.galaxy.training.java.gt.creditcard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.galaxy.training.java.gt.creditcard.entity.CreditCardTransactionEntity;

public interface CreditCardTransactionRepository extends JpaRepository<CreditCardTransactionEntity, Long> {

    List<CreditCardTransactionEntity> findByCardId(Long cardId);
}
