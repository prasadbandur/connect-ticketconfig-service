package com.goeuro.ticketconfig.providerconfig.wrapper;

import com.goeuro.ticketconfig.providerconfig.model.ProviderConfig;
import com.goeuro.ticketconfig.providerconfig.model.SearchConfig;
import lombok.Getter;

import java.util.Optional;

public class SearchConfigWrapper {

  private final ProviderConfig providerConfig;

  @Getter private final AuthConfigWrapper auth;

  SearchConfigWrapper(ProviderConfig providerConfig) {
    this.providerConfig = providerConfig;

    auth = new AuthConfigWrapper(providerConfig);
  }

  public boolean isPassengerData() {
    return getSearchConfig().map(SearchConfig::isPassengerData).orElse(false);
  }

  private Optional<SearchConfig> getSearchConfig() {
    return Optional.ofNullable(providerConfig).map(ProviderConfig::getSearch);
  }

  // TODO
  //  public boolean isMobileTicket(Journey journey) {
  //    return isMobileTicket(extractCarrierCodes(journey));
  //  }

}
