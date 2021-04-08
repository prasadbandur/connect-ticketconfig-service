package com.goeuro.ticketconfig.providerconfig.wrapper;

import com.goeuro.ticketconfig.providerconfig.model.ProviderConfig;
import lombok.Getter;

import java.util.Optional;

public class ProviderConfigWrapper {

  private final ProviderConfig providerConfig;

  @Getter private final SearchConfigWrapper search;
  @Getter private final AdditionalServicesWrapper additionalServices;
  @Getter private final BookingConfigWrapper booking;
  @Getter private final PassengerAgeRangesConfigWrapper passengerAgeRanges;
  @Getter private final CarriersConfigWrapper carriers;

  public ProviderConfigWrapper(ProviderConfig providerConfig) {
    this.providerConfig = providerConfig;

    search = new SearchConfigWrapper(providerConfig);
    additionalServices = new AdditionalServicesWrapper(providerConfig);
    booking = new BookingConfigWrapper(providerConfig);
    passengerAgeRanges = new PassengerAgeRangesConfigWrapper(providerConfig);
    carriers = new CarriersConfigWrapper(providerConfig);
  }

  public String getProviderName() {
    return Optional.ofNullable(providerConfig).map(ProviderConfig::getName).orElse(null);
  }

  public String getBaseUrl() {
    return Optional.ofNullable(providerConfig).map(ProviderConfig::getBaseUrl).orElse(null);
  }

  public String getVersion() {
    return Optional.ofNullable(providerConfig).map(ProviderConfig::getVersion).orElse(null);
  }

  public boolean hasMetaHeader() {
    return Optional.ofNullable(providerConfig).map(ProviderConfig::isMetaHeader).orElse(false);
  }

  public Long getProviderDbId() {
    return Optional.ofNullable(providerConfig).map(ProviderConfig::getDbId).orElse(null);
  }
}
