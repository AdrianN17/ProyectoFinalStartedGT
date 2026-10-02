package pe.edu.galaxy.training.java.sensitive.service.impl;

import pe.edu.galaxy.training.java.sensitive.enums.EncryptionProvider;
import pe.edu.galaxy.training.java.sensitive.service.EncryptService;

/**
 * Implementacion de respaldo usada cuando {@code oms.sensitive.encrypt.enabled=false}.
 *
 * <p>Evita construir un {@code CryptographyClient}/conexion a Azure Key Vault
 * (que valida la conexion de forma eager) cuando el cifrado esta deshabilitado,
 * por ejemplo en entornos de pruebas sin credenciales de {@code az login}.
 * {@link pe.edu.galaxy.training.java.sensitive.processor.SensitiveFieldProcessor}
 * ya evita invocar el cifrado cuando {@code encrypt.enabled=false}, por lo que
 * estos metodos no deberian ejecutarse en ese escenario; se dejan como
 * no-operacion (devuelven el valor tal cual) a modo defensivo.</p>
 */
public class NoOpEncryptServiceImpl implements EncryptService {

    @Override
    public String encrypt(String plainText) {
        return plainText;
    }

    @Override
    public String decrypt(String cipherText) {
        return cipherText;
    }

    @Override
    public boolean supports(EncryptionProvider provider) {
        return true;
    }
}
