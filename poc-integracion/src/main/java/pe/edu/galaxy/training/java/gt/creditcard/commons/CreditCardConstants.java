package pe.edu.galaxy.training.java.gt.creditcard.commons;

public final class CreditCardConstants {

    private CreditCardConstants() {
    }

    public static final String BUSINESS_KEY_CARD = "CREDIT_CARD";
    public static final String BUSINESS_KEY_TRANSACTION = "CREDIT_CARD_TRANSACTION";

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_BLOCKED = "BLOCKED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public static final String TX_APPROVED = "APPROVED";
    public static final String TX_REJECTED = "REJECTED";

    /** Umbral de riesgo (0-100) del servicio de fraude a partir del cual se rechaza la transaccion. */
    public static final int FRAUD_REJECT_THRESHOLD = 80;
}
