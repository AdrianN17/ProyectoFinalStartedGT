package pe.edu.galaxy.training.java.sensitive.aspect;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import pe.edu.galaxy.training.java.sensitive.processor.SensitiveFieldProcessor;

@Aspect
public class SensitiveRepositoryAspect {

    private final SensitiveFieldProcessor processor;

    public SensitiveRepositoryAspect(SensitiveFieldProcessor processor) {
        this.processor = processor;
    }

    @Before("execution(* org.springframework.data.repository.CrudRepository+.save(..)) && args(entity)")
    public void encryptBeforeSave(Object entity) {
        processor.encryptObject(entity);
    }

    @Before("execution(* org.springframework.data.repository.CrudRepository+.saveAll(..)) && args(entities)")
    public void encryptBeforeSaveAll(Iterable<?> entities) {
        if (entities != null) {
            entities.forEach(processor::encryptObject);
        }
    }

    @AfterReturning(
            pointcut = "execution(* org.springframework.data.repository.CrudRepository+.find*(..))",
            returning = "result"
    )
    public void decryptAfterFind(Object result) {
        processor.decryptResult(result);
    }
}
