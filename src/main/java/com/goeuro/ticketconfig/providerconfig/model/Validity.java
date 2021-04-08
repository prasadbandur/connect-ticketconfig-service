package com.goeuro.ticketconfig.providerconfig.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Validity {

  private int minDaysInAdvance;
  private int maxDaysInAdvance;
  private Duration ttl;

  public boolean isValid(LocalDate departureDate, ZoneId departureTimeZone) {
    LocalDate now = ZonedDateTime.now(departureTimeZone).toLocalDate();
    int daysUntilDeparture = now.until(departureDate).getDays();
    return minDaysInAdvance <= daysUntilDeparture
        && (maxDaysInAdvance == 0 || daysUntilDeparture <= maxDaysInAdvance);
  }
}
