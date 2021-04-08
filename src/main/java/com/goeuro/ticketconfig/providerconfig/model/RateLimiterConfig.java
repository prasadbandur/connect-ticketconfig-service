package com.goeuro.ticketconfig.providerconfig.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimiterConfig {

  private boolean enabled;
  private Integer oneWayQuotaAmount;
  private Integer roundTripQuotaAmount;
  private Integer delay;
  private Integer maxDuration;
  private Integer maxRetries;
}
