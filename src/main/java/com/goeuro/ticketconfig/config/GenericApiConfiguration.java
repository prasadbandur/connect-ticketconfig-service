package com.goeuro.ticketconfig.config;

import com.goeuro.ticketconfig.adapter.CachingGenericApiClientProvider;
import com.goeuro.ticketconfig.adapter.GenericApiClientFactory;
import com.goeuro.ticketconfig.adapter.GenericApiClientProvider;
import com.goeuro.ticketconfig.providerconfig.service.ProviderConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GenericApiConfiguration {

  @Bean
  public GenericApiClientProvider genericApiClientProvider(
      ProviderConfigService providerConfigService) {
    return new CachingGenericApiClientProvider(new GenericApiClientFactory(providerConfigService));
  }
}
