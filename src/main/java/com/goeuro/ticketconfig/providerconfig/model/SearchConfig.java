package com.goeuro.ticketconfig.providerconfig.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchConfig {

  /**
   * This flag is an object Boolean (not primitive), because we are still supporting it as an
   * internal configuration (inside Connect+, as fallback, {@link
   * BookingConfiguration#isMobileTicketSupported()}). In doing so, to be able to find out if this
   * property was configured externally or not (inside Provider-Config), we need to have the state:
   * NULL (not configured).
   */
  private Boolean mobileTicket;

  /**
   * This flag is a primitive boolean, because we are not supporting it anymore as an internal
   * configuration (inside Connect+). That means, we have no fallbacks, we are entirely relying on
   * the external configuration (inside Provider-Config).
   */
  private boolean passengerData;

  private Boolean multiplePassengers;

  private Boolean roundTrip;

  private Boolean shareUserDomain;

  private Duration minimumInAdvance;

  private Duration maximumInAdvance;

  private AuthConfig auth;

  private OfferUpsellConfig offerUpsell;

  private RateLimiterConfig rateLimiter;

  private CacheConfig cache;

  private List<CountryWhitelistConfig> countriesWhitelist;
}
