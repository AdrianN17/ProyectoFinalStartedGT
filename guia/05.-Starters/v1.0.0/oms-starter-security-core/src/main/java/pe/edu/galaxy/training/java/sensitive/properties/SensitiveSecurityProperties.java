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
        private EncryptionProvider provider = EncryptionProvider.VAULT;
        private final Vault vault = new Vault();

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

        public Vault getVault() {
            return vault;
        }
    }

    public static class Vault {
        private String uri = "http://localhost:8200";
        private String token;
        private String transitKey = "oms-key";

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getTransitKey() {
            return transitKey;
        }

        public void setTransitKey(String transitKey) {
            this.transitKey = transitKey;
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
