package pe.edu.galaxy.training.java.sensitive.service;

import pe.edu.galaxy.training.java.sensitive.enums.EncryptionProvider;

public interface EncryptService {

    String encrypt(String plainText);

    String decrypt(String cipherText);

    boolean supports(EncryptionProvider provider);
}
