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
public class ConnectionCacheConfig {

  private List<Connection> connections;

  private Duration ttl;
}
