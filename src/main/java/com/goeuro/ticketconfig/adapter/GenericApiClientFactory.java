package com.goeuro.ticketconfig.adapter;

import com.goeuro.coverage.goeuroconnect.client.AbstractApiClient;
import com.goeuro.coverage.goeuroconnect.client.Version;
import com.goeuro.coverage.goeuroconnect.client.v03.GenericApiClientV03;
import com.goeuro.ticketconfig.providerconfig.service.ProviderConfigService;
import com.goeuro.ticketconfig.providerconfig.wrapper.ProviderConfigWrapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenericApiClientFactory implements GenericApiClientProvider {

  private final ProviderConfigService providerConfigService;

  @Override
  public AbstractApiClient getGenericApiForProvider(String provider) {
    ProviderConfigWrapper providerConfig = providerConfigService.getProviderConfigWrapper(provider);
    if (providerConfig.getVersion().equals(Version.v03.name())) {
      GenericApiClientV03 apiClientV03 = new GenericApiClientV03(providerConfig.getBaseUrl());
      providerConfig
          .getSearch()
          .getAuth()
          .getAuthManager()
          .ifPresent(apiClientV03::setAuthorizationManager);
      return apiClientV03;
    }
    throw new IllegalStateException(
        String.format(
            "Api version not supported: %s for provider: %s",
            providerConfig.getVersion(), provider));
  }
}
