package com.goeuro.ticketconfig.providerconfig.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Read the documentation of the schema of provider configurations here:
 * https://github.com/goeuro/provider-config/blob/master/graphql/schema.md
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderConfig {

  private String name;

  private String baseUrl;

  private String version;

  private boolean metaHeader;

  private SearchConfig search = new SearchConfig();

  private AdditionalServicesConfig additionalServices = new AdditionalServicesConfig();

  private BookingConfig booking = new BookingConfig();

  private List<PassengerAgeRangeConfiguration> passengerAgeRanges = new ArrayList<>();

  private List<CarrierConfig> carriers = new ArrayList<>();

  private Long dbId;

  @JsonProperty("provider")
  public void setProvider(Map<String, String> provider) {
    this.name = provider.get("name");
    this.dbId = Long.parseLong(provider.get("dbId"));
  }
}
