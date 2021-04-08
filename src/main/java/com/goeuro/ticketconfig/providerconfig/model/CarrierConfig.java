package com.goeuro.ticketconfig.providerconfig.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarrierConfig {

  private String name;

  private Boolean disabled;

  private Boolean customFare;

  private SearchConfig search = new SearchConfig();

  private BookingConfig booking = new BookingConfig();

  private List<PassengerAgeRangeConfiguration> passengerAgeRanges = new ArrayList<>();

  private List<CustomAdditionalServiceConfiguration> customAdditionalServices = new ArrayList<>();
}
