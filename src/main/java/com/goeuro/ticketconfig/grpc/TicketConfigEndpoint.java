package com.goeuro.ticketconfig.grpc;

import com.goeuro.search2.pi.proto.PiboxTicketConfigurationServiceGrpc;
import com.goeuro.search2.pi.proto.TicketConfigurationRequest;
import com.goeuro.ticketconfig.proto.TicketConfigurationResponse;
import com.goeuro.ticketconfig.service.TicketConfigService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketConfigEndpoint
    extends PiboxTicketConfigurationServiceGrpc.PiboxTicketConfigurationServiceImplBase {

  private final TicketConfigService ticketConfigService;

  @Override
  public void getTicketConfiguration(
      TicketConfigurationRequest request, StreamObserver<TicketConfigurationResponse> observer) {
    String offerStoreId = request.getOfferStoreId();
    log.info("Started to call [getTicketConfiguration] of offer id [{}]", offerStoreId);
    ticketConfigService
        .updateOfferWithTicketConfig(request)
        .doOnSuccess(response -> logSuccess(request, response))
        .onErrorResume(throwable -> createMonoError(request, throwable))
        .doOnError(throwable -> logError(request, throwable))
        .switchIfEmpty(createNotFoundError(request))
        .subscribe(new GrpcCustomSubscriber<>(observer));
  }

  private Mono<TicketConfigurationResponse> createNotFoundError(
      TicketConfigurationRequest request) {
    return Mono.error(
        Status.NOT_FOUND
            .augmentDescription(
                String.format(
                    "TicketConfig for offerStoreId [%s] was not found", request.getOfferStoreId()))
            .asException());
  }

  private void logError(TicketConfigurationRequest request, Throwable throwable) {
    log.error(
        "Could not handle ticket configuration request of offer id [{}]: {}",
        request.getOfferStoreId(),
        throwable);
  }

  private Mono<TicketConfigurationResponse> createMonoError(
      TicketConfigurationRequest request, Throwable throwable) {
    return Mono.error(
        Status.INTERNAL
            .augmentDescription(
                String.format(
                    "Error while updating offer with ticketConfig for offerStoreId %s and provider %s",
                    request.getOfferStoreId(), request.getProviderId()))
            .withCause(throwable)
            .asException());
  }

  private void logSuccess(
      TicketConfigurationRequest request, TicketConfigurationResponse response) {
    log.info(
        "Successfully retrieved ticket configuration of offer id = [{}]. Response: {}",
        request.getOfferStoreId(),
        response);
  }
}
