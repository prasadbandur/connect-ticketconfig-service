package com.goeuro.ticketconfig.providerconfig.wrapper;

import com.goeuro.ticketconfig.providerconfig.model.CountryWhitelistConfig;
import com.goeuro.ticketconfig.providerconfig.model.ProviderConfig;
import com.goeuro.ticketconfig.providerconfig.model.SearchConfig;
import com.google.common.annotations.VisibleForTesting;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@RequiredArgsConstructor
public class CountriesWhitelistConfigWrapper {

  private final ProviderConfig providerConfig;

  public boolean exists() {
    return getCountriesWhitelistConfig().isPresent();
  }

  public List<String> getArrivalCountryCodes(String departureCountryCode) {
    return getCountriesWhitelistConfig()
        .map(SearchConfig::getCountriesWhitelist)
        .flatMap(
            countryWhitelistConfigs ->
                countryWhitelistConfigs.stream()
                    .filter(cc -> cc.getDeparture().equals(departureCountryCode))
                    .findFirst())
        .map(CountryWhitelistConfig::getArrivals)
        .orElse(Collections.emptyList());
  }

  private Optional<SearchConfig> getCountriesWhitelistConfig() {
    return Optional.ofNullable(providerConfig)
        .map(ProviderConfig::getSearch)
        .filter(searchConfig -> isNotEmpty(searchConfig.getCountriesWhitelist()));
  }

  @VisibleForTesting
  public ProviderConfig getProviderConfig() {
    return providerConfig;
  }
}
