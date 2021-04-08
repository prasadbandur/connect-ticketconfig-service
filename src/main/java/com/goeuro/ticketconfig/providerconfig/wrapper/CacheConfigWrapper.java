package com.goeuro.ticketconfig.providerconfig.wrapper;

import com.goeuro.ticketconfig.providerconfig.model.*;
import com.goeuro.ticketconfig.providerconfig.model.CacheKeyConfig.CacheKeyField;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@RequiredArgsConstructor
public class CacheConfigWrapper {

  private final ProviderConfig providerConfig;

  private Optional<CacheConfig> getCacheConfig() {
    return Optional.ofNullable(providerConfig)
        .map(ProviderConfig::getSearch)
        .map(SearchConfig::getCache);
  }

  private List<Validity> getValidity() {
    return getCacheConfig().map(CacheConfig::getValidity).orElse(getFallbackValidity());
  }

  private List<Validity> getFallbackValidity() {
    Validity shortTermValidity =
        Validity.builder().minDaysInAdvance(0).maxDaysInAdvance(14).ttl(Duration.ZERO).build();
    Validity longTermValidity = Validity.builder().minDaysInAdvance(15).ttl(Duration.ZERO).build();
    return Arrays.asList(shortTermValidity, longTermValidity);
  }

  private Duration getFallbackTTL() {
    return getCacheConfig().map(CacheConfig::getFallbackTTL).orElse(Duration.ZERO);
  }

  private Duration getSearchNotPossibleTTL() {
    return getCacheConfig().map(CacheConfig::getSearchNotPossibleTTL).orElse(Duration.ZERO);
  }

  private boolean isCompressionEnabled() {
    return getCacheConfig().map(CacheConfig::getCompression).orElse(false);
  }

  public SearchCacheConfig getConfig() {
    return SearchCacheConfig.builder()
        .validity(getValidity())
        .fallbackTimeToLive(getFallbackTTL())
        .noResultsCacheConfig(getNoResultsConfig())
        .searchNotPossibleTimeToLive(getSearchNotPossibleTTL())
        .compressionEnabled(isCompressionEnabled())
        .build();
  }

  private NoResultsCacheConfig getNoResultsConfig() {
    return getCacheConfig()
        .map(CacheConfig::getNoResults)
        .map(
            noResultsConfig ->
                NoResultsCacheConfig.builder()
                    .ttl(noResultsConfig.getTtl())
                    .overrides(mapConnectionCacheConfig(noResultsConfig.getOverrides()))
                    .build())
        .orElse(null);
  }

  private List<ConnectionCacheConfig> mapConnectionCacheConfig(
      List<ConnectionCacheConfig> connectionCacheConfigs) {
    return Optional.ofNullable(connectionCacheConfigs).orElse(Collections.emptyList()).stream()
        .map(
            config ->
                ConnectionCacheConfig.builder()
                    .ttl(config.getTtl())
                    .connections(mapConnections(config.getConnections()))
                    .build())
        .collect(Collectors.toList());
  }

  private List<Connection> mapConnections(List<Connection> connections) {
    return Optional.ofNullable(connections).orElse(Collections.emptyList()).stream()
        .map(
            conn ->
                Connection.builder()
                    .stationA(conn.getStationA())
                    .stationB(conn.getStationB())
                    .build())
        .collect(Collectors.toList());
  }

  private String getKeySuffix() {
    return getCacheConfig().map(cacheConfig -> cacheConfig.getKey().getSuffix()).orElse(null);
  }

  private List<String> getAbTestExperimentPartitions() {
    return getCacheConfig().map(CacheConfig::getPartitionByAbExperiments).orElse(new ArrayList<>());
  }

  private List<CacheKeyField> getKeyFields() {
    return getCacheConfig()
        .map(CacheConfig::getKey)
        .filter(cacheKeyConfig -> isNotEmpty(cacheKeyConfig.getFields()))
        .map(
            cacheKeyConfig ->
                cacheKeyConfig.getFields().stream()
                    .map(f -> CacheKeyField.valueOf(f.name()))
                    .collect(Collectors.toList()))
        .orElse(Collections.emptyList());
  }
}
