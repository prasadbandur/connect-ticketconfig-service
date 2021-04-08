package com.goeuro.ticketconfig.adapter;

import com.goeuro.coverage.goeuroconnect.client.exception.GenericApiException;
import com.goeuro.coverage.goeuroconnect.client.v03.GenericApiClientV03;
import com.goeuro.coverage.goeuroconnect.model.v03.SearchRequest;
import com.goeuro.coverage.goeuroconnect.model.v03.SearchResponse;
import com.goeuro.coverage.goeuroconnect.model.v03.TicketConfigRequest;
import com.goeuro.coverage.goeuroconnect.model.v03.TicketConfigResponse;
import com.google.common.base.Stopwatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static net.logstash.logback.marker.Markers.append;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdapterClient {

  private static final String PROVIDER_DURATION = "pibox.provider_duration";

  private final GenericApiClientProvider genericApiClientProvider;

  public SearchResponse search(
      String serviceProvider,
      SearchRequest searchRequest,
      Pair<String, String> searchIdHeader,
      Pair<String, String> abTestParametersHeader) {
    try {
      return getSearchResponse(
          serviceProvider, searchRequest, searchIdHeader, abTestParametersHeader);
    } catch (GenericApiException e) {
      log.error(
          String.format("Exception while getting search data for provider: %s", serviceProvider),
          e);
      throw e;
    }
  }

  private SearchResponse getSearchResponse(
      String serviceProvider,
      SearchRequest searchRequest,
      Pair<String, String> searchIdHeader,
      Pair<String, String> abTestParametersHeader) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    log.info("Starting provider call for provider {}", serviceProvider);
    GenericApiClientV03 genericApiClient =
        (GenericApiClientV03) genericApiClientProvider.getGenericApiForProvider(serviceProvider);
    SearchResponse searchResponse =
        genericApiClient.search(searchRequest, searchIdHeader, abTestParametersHeader);
    long durationInMillis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
    log.info(
        append(PROVIDER_DURATION, durationInMillis),
        "Completed provider call for provider {} in {} ms",
        serviceProvider,
        durationInMillis);
    return searchResponse;
  }

  public TicketConfigResponse getTicketConfigResponse(
      String serviceProvider,
      TicketConfigRequest ticketConfigRequest,
      Pair<String, String> header) {
    GenericApiClientV03 genericApiClient =
        (GenericApiClientV03) genericApiClientProvider.getGenericApiForProvider(serviceProvider);
    return genericApiClient.ticketConfig(ticketConfigRequest, header);
  }
}
