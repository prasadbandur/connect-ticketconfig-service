package com.goeuro.ticketconfig.providerconfig.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaggageItemConfig {

  private String id;
  private BaggageType type;
  private DimensionConfig maxDimension;
  private WeightConfig maxWeight;

  public enum BaggageType {
    CABIN,
    SMALL_CABIN,
    CHECKED,
    HAND,
    STROLLER
  }
}
