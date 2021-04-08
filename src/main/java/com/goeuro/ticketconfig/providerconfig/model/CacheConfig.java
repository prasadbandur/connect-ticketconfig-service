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
public class CacheConfig {

  private List<Validity> validity;
  private Duration fallbackTTL;
  private NoResultsCacheConfig noResults;
  private Duration searchNotPossibleTTL;
  private Boolean compression;
  private CacheKeyConfig key;
  private List<String> partitionByAbExperiments;
}
