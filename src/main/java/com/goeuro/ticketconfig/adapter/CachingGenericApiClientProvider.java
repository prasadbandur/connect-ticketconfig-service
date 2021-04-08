package com.goeuro.ticketconfig.adapter;

import com.goeuro.coverage.goeuroconnect.client.AbstractApiClient;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
public class CachingGenericApiClientProvider implements GenericApiClientProvider {

  private final Map<String, AbstractApiClient> providerClients = new ConcurrentHashMap<>();

  private final GenericApiClientProvider genericApiClientFactory;

  @Override
  public synchronized AbstractApiClient getGenericApiForProvider(String provider) {
    if (StringUtils.isEmpty(provider)) {
      throw new IllegalArgumentException(
          "Missing Provider name, to initialize the Generic Api Client");
    }
    return providerClients.computeIfAbsent(
        provider, genericApiClientFactory::getGenericApiForProvider);
  }
}
