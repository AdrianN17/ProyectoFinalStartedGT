package pe.edu.galaxy.training.java.gt.creditcard.fraud.generated;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.constraints.NotNull;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * FraudCheckResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-10-02T12:46:18.091880954-05:00[America/Lima]", comments = "Generator version: 7.25.0")
public class FraudCheckResponse {

  private Integer riskScore;

  public FraudCheckResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public FraudCheckResponse(Integer riskScore) {
    this.riskScore = riskScore;
  }

  public FraudCheckResponse riskScore(Integer riskScore) {
    this.riskScore = riskScore;
    return this;
  }

  /**
   * Puntaje de riesgo 0-100. Umbrales usados por los simuladores de referencia: amount > 3000 -> 90 (alto), amount > 1000 -> 50 (medio), en otro caso -> 10 (bajo). 
   * minimum: 0
   * maximum: 100
   * @return riskScore
   */
  @NotNull
  @JsonProperty("riskScore")
  public Integer getRiskScore() {
    return riskScore;
  }

  @JsonProperty("riskScore")
  public void setRiskScore(Integer riskScore) {
    this.riskScore = riskScore;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FraudCheckResponse fraudCheckResponse = (FraudCheckResponse) o;
    return Objects.equals(this.riskScore, fraudCheckResponse.riskScore);
  }

  @Override
  public int hashCode() {
    return Objects.hash(riskScore);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FraudCheckResponse {\n");
    sb.append("    riskScore: ").append(toIndentedString(riskScore)).append("\n");
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

