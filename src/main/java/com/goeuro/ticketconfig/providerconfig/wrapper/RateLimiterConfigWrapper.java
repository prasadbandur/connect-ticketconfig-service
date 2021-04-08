package com.goeuro.ticketconfig.providerconfig.wrapper;

import com.goeuro.ticketconfig.providerconfig.model.ProviderConfig;
import com.goeuro.ticketconfig.providerconfig.model.RateLimiterConfig;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class RateLimiterConfigWrapper {

  private final ProviderConfig providerConfig;

  private Optional<RateLimiterConfig> getRateLimiterConfig() {
    return Optional.ofNullable(providerConfig)
        .map(p -> providerConfig.getSearch().getRateLimiter());
  }

  public boolean isEnabled() {
    return getRateLimiterConfig().map(RateLimiterConfig::isEnabled).orElse(false);
  }

  public int getOneWayQuotaAmount() {
    return getRateLimiterConfig().map(RateLimiterConfig::getOneWayQuotaAmount).orElse(1);
  }

  public int getRoundTripQuotaAmount() {
    return getRateLimiterConfig().map(RateLimiterConfig::getRoundTripQuotaAmount).orElse(2);
  }

  public Integer getDelay() {
    return getRateLimiterConfig().map(RateLimiterConfig::getDelay).orElse(null);
  }

  public Integer getMaxRetries() {
    return getRateLimiterConfig().map(RateLimiterConfig::getMaxRetries).orElse(null);
  }

  public Integer getMaxDuration() {
    return getRateLimiterConfig().map(RateLimiterConfig::getMaxDuration).orElse(null);
  }
}
