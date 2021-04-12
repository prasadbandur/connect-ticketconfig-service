package com.goeuro.ticketconfig.offerstore;

import com.goeuro.coverage.offer.store.protobuf.OfferStoreDocument;
import com.goeuro.coverage.offer.store.protobuf.OfferStoreServiceGrpc;
import com.goeuro.coverage.offer.store.protobuf.PutBookingOfferResponse;
import com.goeuro.search2.pi.proto.OfferDetailsQuery;
import com.goeuro.search2.pi.proto.OfferDetailsResponse;
import com.goeuro.search2.pi.proto.PiboxOfferDetailsServiceGrpc;
import com.goeuro.ticketconfig.config.ConnectOfferStoreProperties;
import com.goeuro.ticketconfig.utils.ReactiveUtil;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectOfferStoreGrpcClient {

  private final ConnectOfferStoreProperties storeProperties;
  private PiboxOfferDetailsServiceGrpc.PiboxOfferDetailsServiceBlockingStub offerDetailsStub;
  private OfferStoreServiceGrpc.OfferStoreServiceBlockingStub offerStoreStub;

  @PostConstruct
  private void init() {
    var channel = getManagedChannel();
    offerDetailsStub = PiboxOfferDetailsServiceGrpc.newBlockingStub(channel);
    offerStoreStub = OfferStoreServiceGrpc.newBlockingStub(channel);
  }

  public Mono<PutBookingOfferResponse> putOffers(OfferStoreDocument storeDocument) {
    return Mono.fromCallable(() -> offerStoreStub.putOffers(storeDocument))
            .doOnSuccess(
                    response ->
                            log.info(
                                    "Successfully saved details for offerStoreId: {} provider: {} with status: {} ",
                                    storeDocument.getMetadata().getProviderQueryId(),
                                    storeDocument.getMetadata().getProviderDbId(),
                                    response.getStatus()))
            .doOnError(
                    throwable ->
                            log.error(
                                    String.format(
                                            "Error while saving offer for offerStoreId: %s and provider: %s",
                                            storeDocument.getMetadata().getProviderQueryId(),
                                            storeDocument.getMetadata().getProviderDbId()),
                                    throwable))
            .onErrorResume(throwable -> createPutError(throwable, storeDocument));
  }

  public Mono<OfferDetailsResponse> getOfferDetails(OfferDetailsQuery query) {
    return Mono.fromCallable(() -> offerDetailsStub.getOfferDetails(query))
        .doOnSuccess(
            response ->
                log.info(
                    "Successfully retrieved details for offerStoreId: {} provider: {} with offerDetails: {} ",
                    query.getOfferStoreId(),
                    query.getProviderId(),
                    response.getMessage()))
        .doOnError(
            throwable ->
                log.error(
                    "Error while fetching OfferDetails for offerStoreId: {} and provider: {}",
                    query.getOfferStoreId(),
                    query.getProviderId(),
                    throwable))
        .onErrorResume(throwable -> createGetError(throwable, query))
        .switchIfEmpty(createNotfoundError(query));
  }

  private ManagedChannel getManagedChannel() {
    return ManagedChannelBuilder.forAddress(storeProperties.getHost(), storeProperties.getPort())
        .usePlaintext()
        .build();
  }

  private Mono<OfferDetailsResponse> createNotfoundError(OfferDetailsQuery query) {
    return ReactiveUtil.createNotFoundMonoError(
        String.format(
            "OfferDetails not found for offerStoreId: %s and provider: %s",
            query.getOfferStoreId(), query.getProviderId()));
  }

  private Mono<OfferDetailsResponse> createGetError(Throwable throwable, OfferDetailsQuery query) {
    return ReactiveUtil.createMonoError(
        throwable,
        String.format(
            "Error while fetching offer for offerStoreId: %s and provider: %s",
            query.getOfferStoreId(), query.getProviderId()));
  }

  private Mono<PutBookingOfferResponse> createPutError(
          Throwable throwable, OfferStoreDocument storeDocument) {
    return ReactiveUtil.createMonoError(
            throwable,
            String.format(
                    "Error while saving offer for offerStoreId: %s and provider: %s",
                    storeDocument.getMetadata().getProviderQueryId(),
                    storeDocument.getMetadata().getProviderDbId()));
  }
}
