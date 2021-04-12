package com.goeuro.ticketconfig.providerconfig.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheKeyConfig {

  private String suffix;
  private List<CacheKeyField> fields;

  public enum CacheKeyField {
    ARRIVAL_STATION_CODE,
    DEPARTURE_STATION_CODE,
    DEPARTURE_DATE,
    RETURN_DATE,
    PASSENGERS
  }
}
