package com.goeuro.ticketconfig.providerconfig.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.goeuro.coverage.goeuroconnect.model.v03.PassengerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * How to override the passenger age ranges in your provider YAML file:
 *
 * <p>passengerAgeRanges: - type: infant min: 0 max: 2 - type: child min: 3 max: 11 - type: adult
 * min: 12 max: 100
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerAgeRangeConfiguration {

  private PassengerType type;
  private int min;
  private int max;

  @JsonProperty("type")
  public void setType(String sType) {
    this.type = PassengerType.valueOf(sType.toLowerCase());
  }
}
