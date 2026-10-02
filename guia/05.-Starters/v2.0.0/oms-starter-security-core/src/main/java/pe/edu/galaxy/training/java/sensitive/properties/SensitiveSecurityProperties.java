package pe.edu.galaxy.training.java.sensitive.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import pe.edu.galaxy.training.java.sensitive.enums.EncryptionProvider;

@ConfigurationProperties(prefix = "oms.sensitive")
public class SensitiveSecurityProperties {

    private boolean enabled = true;
    private final Encrypt encrypt = new Encrypt();
    private final Mask mask = new Mask();
    private final Audit audit = new Audit();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Encrypt getEncrypt() {
        return encrypt;
    }

    public Mask getMask() {
        return mask;
    }

    public Audit getAudit() {
        return audit;
    }

    public static class Encrypt {
        private boolean enabled = true;
        private EncryptionProvider provider = EncryptionProvider.AZURE_KEY_VAULT;
        private final AzureKeyVault azureKeyVault = new AzureKeyVault();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public EncryptionProvider getProvider() {
            return provider;
        }

        public void setProvider(EncryptionProvider provider) {
            this.provider = provider;
        }

        public AzureKeyVault getAzureKeyVault() {
            return azureKeyVault;
        }
    }

    public static class AzureKeyVault {
        private String vaultUrl = "https://CHANGE-ME.vault.azure.net";
        private String keyName = "oms-key";
        private String keyVersion;
        private String algorithm = "RSA-OAEP-256";

        public String getVaultUrl() {
            return vaultUrl;
        }

        public void setVaultUrl(String vaultUrl) {
            this.vaultUrl = vaultUrl;
        }

        public String getKeyName() {
            return keyName;
        }

        public void setKeyName(String keyName) {
            this.keyName = keyName;
        }

        public String getKeyVersion() {
            return keyVersion;
        }

        public void setKeyVersion(String keyVersion) {
            this.keyVersion = keyVersion;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }
    }

    public static class Mask {
        private boolean enabled = true;
        private boolean enabledByDefault = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabledByDefault() {
            return enabledByDefault;
        }

        public void setEnabledByDefault(boolean enabledByDefault) {
            this.enabledByDefault = enabledByDefault;
        }
    }

    public static class Audit {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
