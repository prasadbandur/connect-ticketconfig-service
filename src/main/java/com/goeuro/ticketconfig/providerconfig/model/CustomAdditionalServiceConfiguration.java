package com.goeuro.ticketconfig.providerconfig.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomAdditionalServiceConfiguration {

  private CustomAdditionalServiceConfigurationType type;
  private Boolean customLabel;

  public enum CustomAdditionalServiceConfigurationType {
    BAGGAGE,
    VEHICLE,
    SEAT_SELECTION
  }
}
