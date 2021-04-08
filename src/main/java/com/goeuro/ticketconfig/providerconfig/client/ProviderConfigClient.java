package com.goeuro.ticketconfig.providerconfig.client;

import com.goeuro.ticketconfig.config.ProviderConfigEnvironment;
import com.goeuro.ticketconfig.http.WebClientFactory;
import com.goeuro.ticketconfig.http.model.WebClientConfiguration;
import com.goeuro.ticketconfig.providerconfig.model.ProviderConfig;
import com.omio.coverage.connect.providerconfig.model.ProviderConfigRequest;
import com.omio.coverage.connect.providerconfig.model.ProviderConfigResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

import static com.couchbase.client.deps.io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderConfigClient {

  private static final String USER_AGENT = "connect-search-service";
  private static final ProviderConfigRequest REQUEST =
      ProviderConfigRequest.builder()
          .query(
              "query {"
                  + "  connects {"
                  + "    provider {"
                  + "       dbId"
                  + "       name"
                  + "    }"
                  + "    baseUrl"
                  + "    version"
                  + "    metaHeader"
                  + "    search {"
                  + "      mobileTicket"
                  + "      multiplePassengers"
                  + "      roundTrip"
                  + "      passengerData"
                  + "      shareUserDomain"
                  + "      minimumInAdvance"
                  + "      maximumInAdvance"
                  + "      offerUpsell {"
                  + "        classRanks {"
                  + "          name"
                  + "          rank"
                  + "        }"
                  + "      }"
                  + "      rateLimiter {"
                  + "        enabled"
                  + "        oneWayQuotaAmount"
                  + "        roundTripQuotaAmount"
                  + "        delay"
                  + "        maxDuration"
                  + "        maxRetries"
                  + "      }"
                  + "      cache {"
                  + "        validity {"
                  + "          minDaysInAdvance"
                  + "          maxDaysInAdvance"
                  + "          ttl"
                  + "        }"
                  + "        fallbackTTL"
                  + "        noResults {"
                  + "          ttl"
                  + "          overrides {"
                  + "            ttl"
                  + "            connections {"
                  + "              stationA"
                  + "              stationB"
                  + "            }"
                  + "          }"
                  + "        }"
                  + "        searchNotPossibleTTL"
                  + "        compression"
                  + "        key {"
                  + "          suffix"
                  + "          fields"
                  + "        }"
                  + "      }"
                  + "      countriesWhitelist {"
                  + "        departure"
                  + "        arrivals"
                  + "      }"
                  + "      auth {"
                  + "        type"
                  + "        static {"
                  + "          header"
                  + "        }"
                  + "        token {"
                  + "          endpointUrl"
                  + "          type"
                  + "          fieldName"
                  + "          expirationFieldName"
                  + "          client"
                  + "          secret"
                  + "          request {"
                  + "            username"
                  + "            password"
                  + "            apiKey"
                  + "          }"
                  + "        }"
                  + "      }"
                  + "    }"
                  + "    discountCards {"
                  + "      key"
                  + "      name"
                  + "      passengerType"
                  + "      cards {"
                  + "        code"
                  + "        description"
                  + "        appliesToAllPassengers"
                  + "      }"
                  + "    }"
                  + "    passengerAgeRanges {"
                  + "      type"
                  + "      min"
                  + "      max"
                  + "    }"
                  + "    additionalServices {"
                  + "      enabled"
                  + "      baggageItems {"
                  + "        id"
                  + "        type"
                  + "        maxDimension {"
                  + "          height"
                  + "          width"
                  + "          length"
                  + "        }"
                  + "        maxWeight {"
                  + "          weight"
                  + "        }"
                  + "      }"
                  + "    }"
                  + "    booking {"
                  + "      onSite"
                  + "    }"
                  + "    carriers {"
                  + "      name"
                  + "      disabled"
                  + "      customFare"
                  + "      search {"
                  + "        mobileTicket"
                  + "      }"
                  + "      booking {"
                  + "        onSite"
                  + "      }"
                  + "      passengerAgeRanges {"
                  + "        type"
                  + "        min"
                  + "        max"
                  + "      }"
                  + "      customAdditionalServices {"
                  + "        type"
                  + "        customLabel"
                  + "      }"
                  + "    }"
                  + "  }"
                  + "}")
          .build();

  private WebClient webClient;
  private final WebClientFactory webClientFactory;
  private final ProviderConfigEnvironment providerConfigEnvironment;

  @PostConstruct
  public void init() {
    this.webClient =
        webClientFactory.createWebClient(
            WebClientConfiguration.builder()
                .host(providerConfigEnvironment.getHost())
                .connectTimeout(providerConfigEnvironment.getConnectionTimeout())
                .readTimeout(providerConfigEnvironment.getReadTimeout())
                .headers(
                    Map.of(
                        HttpHeaders.USER_AGENT,
                        USER_AGENT,
                        HttpHeaders.CONTENT_TYPE,
                        APPLICATION_JSON))
                .build());
  }

  public Mono<List<ProviderConfig>> getAll() {
    return request()
        .map(
            providerConfigProviderConfigResponse ->
                providerConfigProviderConfigResponse.getData().getConnects())
        .onErrorResume(Mono::error)
        .doOnError(
            throwable -> log.error("Error occurred while calling provider config ", throwable));
  }

  private Mono<ProviderConfigResponse<ProviderConfig>> request() {
    return webClient
        .post()
        .body(Mono.just(REQUEST), new ParameterizedTypeReference<>() {})
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<>() {});
  }
}
