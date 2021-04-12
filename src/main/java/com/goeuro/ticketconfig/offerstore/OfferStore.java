package com.goeuro.ticketconfig.offerstore;

import com.goeuro.coverage.offer.store.protobuf.BookingOffer;
import com.goeuro.search2.pi.proto.OfferDetailsQuery;
import com.goeuro.search2.pi.proto.OfferDetailsResponse;
import com.goeuro.ticketconfig.utils.ReactiveUtil;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class OfferStore {

  private static final JsonFormat.Parser JSON_PARSER = JsonFormat.parser();

  private final ConnectOfferStoreGrpcClient storeGrpcClient;

  public Mono<OfferDetailsResponse> getOffer(OfferDetailsQuery query) {
    log.info(
        "Processing get offer for offerStoreId: {} and provider: {}",
        query.getOfferStoreId(),
        query.getProviderId());
    return storeGrpcClient
        .getOfferDetails(query)
        .onErrorResume(
            throwable ->
                ReactiveUtil.createMonoError(
                    throwable,
                    String.format(
                        "Error fetching offer with offerStoreId: %s and provider: %s",
                        query.getOfferStoreId(), query.getProviderId())))
        .switchIfEmpty(
            ReactiveUtil.createNotFoundMonoError(
                String.format(
                    "Offer not found with offerStoreId: %s and provider: %s",
                    query.getOfferStoreId(), query.getProviderId())));
  }

  public Mono<BookingOffer> getBookingOffer(OfferDetailsQuery query) {
    return getOffer(query)
            .map(response -> mapBookingOffer(response, query.getOfferStoreId()))
            .doOnError(
                    throwable ->
                            log.error(
                                    "Error parsing OfferDetailsResponse json for offerStoreId: {}",
                                    query.getOfferStoreId(),
                                    throwable))
            .onErrorResume(Mono::error);
  }

  private BookingOffer mapBookingOffer(
          OfferDetailsResponse offerDetailsResponse, String offerStoreId) {
    try {
      var offerBuilder = BookingOffer.newBuilder();
      JSON_PARSER.merge(offerDetailsResponse.getMessage(), offerBuilder);
      return offerBuilder.build();
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException(
              "Something went wrong trying to parse OfferDetails to BookingOffer for offerStoreId: "
                      + offerStoreId);
    }
  }
}
