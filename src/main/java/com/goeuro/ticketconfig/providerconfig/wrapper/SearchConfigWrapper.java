package com.goeuro.ticketconfig.providerconfig.wrapper;

import com.goeuro.ticketconfig.providerconfig.model.*;
import lombok.Getter;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.goeuro.ticketconfig.providerconfig.wrapper.CarriersConfigWrapper.*;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

public class SearchConfigWrapper {

  private final ProviderConfig providerConfig;

  @Getter private final AuthConfigWrapper auth;
  @Getter private final RateLimiterConfigWrapper rateLimiter;
  @Getter private final CacheConfigWrapper cache;
  @Getter private final CountriesWhitelistConfigWrapper countriesWhitelist;

  SearchConfigWrapper(ProviderConfig providerConfig) {
    this.providerConfig = providerConfig;

    auth = new AuthConfigWrapper(providerConfig);
    rateLimiter = new RateLimiterConfigWrapper(providerConfig);
    cache = new CacheConfigWrapper(providerConfig);
    countriesWhitelist = new CountriesWhitelistConfigWrapper(providerConfig);
  }

  public Duration getMaximumInAdvance() {
    return getSearchConfig().map(SearchConfig::getMaximumInAdvance).orElse(Duration.ZERO);
  }

  public Duration getMinimumInAdvance() {
    Duration minimumInAdvanceDuration =
        getSearchConfig().map(SearchConfig::getMinimumInAdvance).orElse(null);
    return isNull(minimumInAdvanceDuration) ? Duration.ZERO : minimumInAdvanceDuration;
  }

  public Integer getClassRank(String seatClass) {
    return getSearchConfig()
        .map(SearchConfig::getOfferUpsell)
        .map(OfferUpsellConfig::getClassRanks)
        .flatMap(
            classRankConfigs ->
                classRankConfigs.stream()
                    .filter(classRankConfig -> classRankConfig.getName().equals(seatClass))
                    .map(ClassRankConfig::getRank)
                    .findFirst())
        .orElse(null);
  }

  public boolean isPassengerData() {
    return getSearchConfig().map(SearchConfig::isPassengerData).orElse(false);
  }

  public boolean isMobileTicket() {
    return getSearchConfig().map(SearchConfig::getMobileTicket).orElse(false);
  }

  public boolean isMobileTicket(String carrierCode) {
    return getCarrierConfig(providerConfig, carrierCode)
        .map(CarrierConfig::getSearch)
        .map(SearchConfig::getMobileTicket)
        .orElse(isMobileTicket());
  }

  public boolean isMobileTicket(List<String> carrierCodes) {
    return isEmpty(carrierCodes)
        ? isMobileTicket()
        : carrierCodes.stream().allMatch(this::isMobileTicket);
  }

  public boolean isMultiplePassengers() {
    return getSearchConfig().map(SearchConfig::getMultiplePassengers).orElse(false);
  }

  public boolean isRoundTrip() {
    return getSearchConfig().map(SearchConfig::getRoundTrip).orElse(false);
  }

  public boolean shouldShareUserDomain() {
    return getSearchConfig().map(SearchConfig::getShareUserDomain).orElse(false);
  }

  private Optional<SearchConfig> getSearchConfig() {
    return Optional.ofNullable(providerConfig).map(ProviderConfig::getSearch);
  }

  // TODO
  //  public boolean isMobileTicket(Journey journey) {
  //    return isMobileTicket(extractCarrierCodes(journey));
  //  }

}
