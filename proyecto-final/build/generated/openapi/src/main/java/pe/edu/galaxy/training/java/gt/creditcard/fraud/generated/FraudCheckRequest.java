package pe.edu.galaxy.training.java.gt.creditcard.fraud.generated;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.math.BigDecimal;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.constraints.NotNull;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * FraudCheckRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-10-02T12:46:18.091880954-05:00[America/Lima]", comments = "Generator version: 7.25.0")
public class FraudCheckRequest {

  private Long cardId;

  private String merchant;

  private BigDecimal amount;

  public FraudCheckRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public FraudCheckRequest(Long cardId, String merchant, BigDecimal amount) {
    this.cardId = cardId;
    this.merchant = merchant;
    this.amount = amount;
  }

  public FraudCheckRequest cardId(Long cardId) {
    this.cardId = cardId;
    return this;
  }

  /**
   * Get cardId
   * @return cardId
   */
  @NotNull
  @JsonProperty("cardId")
  public Long getCardId() {
    return cardId;
  }

  @JsonProperty("cardId")
  public void setCardId(Long cardId) {
    this.cardId = cardId;
  }

  public FraudCheckRequest merchant(String merchant) {
    this.merchant = merchant;
    return this;
  }

  /**
   * Get merchant
   * @return merchant
   */
  @NotNull
  @JsonProperty("merchant")
  public String getMerchant() {
    return merchant;
  }

  @JsonProperty("merchant")
  public void setMerchant(String merchant) {
    this.merchant = merchant;
  }

  public FraudCheckRequest amount(BigDecimal amount) {
    this.amount = amount;
    return this;
  }

  /**
   * Monto de la transaccion (BigDecimal en el modelo Java generado).
   * @return amount
   */
  @NotNull
  @JsonProperty("amount")
  public BigDecimal getAmount() {
    return amount;
  }

  @JsonProperty("amount")
  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FraudCheckRequest fraudCheckRequest = (FraudCheckRequest) o;
    return Objects.equals(this.cardId, fraudCheckRequest.cardId) &&
        Objects.equals(this.merchant, fraudCheckRequest.merchant) &&
        Objects.equals(this.amount, fraudCheckRequest.amount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardId, merchant, amount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FraudCheckRequest {\n");
    sb.append("    cardId: ").append(toIndentedString(cardId)).append("\n");
    sb.append("    merchant: ").append(toIndentedString(merchant)).append("\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(@Nullable Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }
}

