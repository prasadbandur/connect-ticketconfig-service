package com.goeuro.ticketconfig.providerconfig.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.goeuro.ticketconfig.providerconfig.model.NoResultsCacheConfig;
import com.goeuro.ticketconfig.providerconfig.model.Validity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/** Configuration for the {@link //CachingSearchService}. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true, chain = true)
public class SearchCacheConfig {

  /** The TTL for results, differentiated by days in the future. */
  @JsonProperty("validity")
  private List<Validity> validity;

  private NoResultsCacheConfig noResultsCacheConfig;

  /** The default TTL for searches that cannot be executed. */
  @JsonProperty("searchNotPossibleTimeToLive")
  private Duration searchNotPossibleTimeToLive;

  /** The Fallback TTL used to return cached results if live search failed. */
  @JsonProperty("FallbackTimeToLive")
  private Duration fallbackTimeToLive;

  /** Whether to use compression on the cached search results */
  @JsonProperty("compressionEnabled")
  private boolean compressionEnabled;

  public static SearchCacheConfig devConfig() {
    return SearchCacheConfig.builder()
        .validity(getDevValidity())
        .fallbackTimeToLive(Duration.ofHours(12))
        .build();
  }

  private static List<Validity> getDevValidity() {
    Validity shortTermValidity =
        Validity.builder()
            .minDaysInAdvance(0)
            .maxDaysInAdvance(14)
            .ttl(Duration.ofHours(1))
            .build();
    Validity longTermValidity =
        Validity.builder().minDaysInAdvance(15).ttl(Duration.ofHours(6)).build();
    return Arrays.asList(shortTermValidity, longTermValidity);
  }
}
