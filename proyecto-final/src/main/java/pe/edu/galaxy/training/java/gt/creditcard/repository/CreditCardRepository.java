package pe.edu.galaxy.training.java.gt.creditcard.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.galaxy.training.java.gt.creditcard.entity.CreditCardEntity;

public interface CreditCardRepository extends JpaRepository<CreditCardEntity, Long> {
}
