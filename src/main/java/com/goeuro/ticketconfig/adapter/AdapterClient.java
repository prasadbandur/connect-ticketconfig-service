package com.goeuro.ticketconfig.adapter;

import com.goeuro.coverage.goeuroconnect.client.v03.GenericApiClientV03;
import com.goeuro.coverage.goeuroconnect.model.v03.TicketConfigRequest;
import com.goeuro.coverage.goeuroconnect.model.v03.TicketConfigResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdapterClient {

  private final GenericApiClientProvider genericApiClientProvider;

  public TicketConfigResponse getTicketConfigResponse(
      String serviceProvider,
      TicketConfigRequest ticketConfigRequest,
      Pair<String, String> header) {
    GenericApiClientV03 genericApiClient =
        (GenericApiClientV03) genericApiClientProvider.getGenericApiForProvider(serviceProvider);
    return genericApiClient.ticketConfig(ticketConfigRequest, header);
  }
}
