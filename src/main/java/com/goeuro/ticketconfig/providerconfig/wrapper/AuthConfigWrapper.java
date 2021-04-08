package com.goeuro.ticketconfig.providerconfig.wrapper;

import com.goeuro.coverage.goeuroconnect.client.auth.IAuthManager;
import com.goeuro.coverage.goeuroconnect.client.auth.staticheader.StaticHeaderAuthConfig;
import com.goeuro.coverage.goeuroconnect.client.auth.token.TokenAuthConfig;
import com.goeuro.ticketconfig.providerconfig.model.AuthConfig;
import com.goeuro.ticketconfig.providerconfig.model.ProviderConfig;
import com.goeuro.ticketconfig.providerconfig.model.SearchConfig;
import lombok.AllArgsConstructor;

import java.util.Optional;

import static com.goeuro.ticketconfig.providerconfig.model.AuthConfig.AuthType.*;
import static java.util.Objects.nonNull;

@AllArgsConstructor
public class AuthConfigWrapper {

  private final ProviderConfig providerConfig;

  public Optional<IAuthManager> getAuthManager() {
    return Optional.ofNullable(providerConfig)
        .map(ProviderConfig::getSearch)
        .map(SearchConfig::getAuth)
        .flatMap(this::getAuthManager);
  }

  private Optional<IAuthManager> getAuthManager(AuthConfig authConfig) {
    TokenAuthConfig token = authConfig.getToken();
    if (nonNull(token) && authConfig.getType() == TOKEN) {
      return Optional.ofNullable(token.getAuthManager());
    }
    StaticHeaderAuthConfig staticHeader = authConfig.getStaticHeader();
    if (nonNull(staticHeader) && authConfig.getType() == STATIC) {
      return Optional.ofNullable(staticHeader.getAuthManager());
    }
    return Optional.empty();
  }
}
