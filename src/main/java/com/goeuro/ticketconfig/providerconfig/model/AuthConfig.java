package com.goeuro.ticketconfig.providerconfig.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.goeuro.coverage.goeuroconnect.client.auth.staticheader.StaticHeaderAuthConfig;
import com.goeuro.coverage.goeuroconnect.client.auth.token.TokenAuthConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthConfig {

  private AuthType type;

  private TokenAuthConfig token;

  @JsonProperty("static")
  private StaticHeaderAuthConfig staticHeader;

  public enum AuthType {
    TOKEN,
    STATIC
  }
}
