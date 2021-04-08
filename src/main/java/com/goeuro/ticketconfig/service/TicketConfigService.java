package com.goeuro.ticketconfig.service;

import com.goeuro.coverage.goeuroconnect.model.v03.TicketConfigRequest;
import com.goeuro.coverage.goeuroconnect.model.v03.TicketConfigResponse;
import com.goeuro.coverage.offer.store.protobuf.BookingOffer;
import com.goeuro.coverage.offer.store.protobuf.OfferStoreDocument;
import com.goeuro.search2.model.proto.ActionRules;
import com.goeuro.search2.model.proto.Fare;
import com.goeuro.search2.pi.proto.OfferDetailsQuery;
import com.goeuro.search2.pi.proto.PiboxUpdateOfferRequest;
import com.goeuro.search2.pi.proto.PiboxUpdateOfferResponse;
import com.goeuro.search2.pi.proto.TicketConfigurationRequest;
import com.goeuro.ticketconfig.adapter.AdapterClient;
import com.goeuro.ticketconfig.mapper.*;
import com.goeuro.ticketconfig.offerstore.OfferStore;
import com.goeuro.ticketconfig.proto.TicketConfigurationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketConfigService {

  private final OfferStore offerStore;
  private final AdapterClient adapterClient;
  private final PassengerDataMapper passengerDataMapper;
  private final TicketConfigurationMapper ticketConfigurationMapper;
  private final ConnectSolutionMapper solutionMapper;
  private final ConnectOfferMapper offerMapper;
  private final ProtobufOfferMapper protobufOfferMapper;

  public Mono<TicketConfigurationResponse> updateOfferWithTicketConfig(
      TicketConfigurationRequest request) {
    log.info("Handling ticket config request: {}", request);

    var query = createOfferDetailsQuery(request.getProviderId(), request.getOfferStoreId());

    return offerStore
        .getBookingOffer(query)
        .map(
            offer -> {
              if (offer.getMetadata().getTicketConfigAvailable()) {
                TicketConfigRequest ticketConfigRequest = createQuery(offer, false);
                TicketConfigResponse ticketConfigResponse =
                    adapterClient.getTicketConfigResponse(
                        request.getProviderId(), ticketConfigRequest, null);
                return ticketConfigurationMapper.mapResponse(
                    ticketConfigResponse,
                    request.getProviderId(),
                    offer.getOutboundSegment().getCarrierCode());
              }
              return TicketConfigurationResponse.getDefaultInstance();
            });
  }

  public Mono<PiboxUpdateOfferResponse> refreshOfferWithTicketConfig(
      PiboxUpdateOfferRequest offerRequest) {
    var query =
        createOfferDetailsQuery(offerRequest.getProviderId(), offerRequest.getOfferStoreId());
    return offerStore
        .getBookingOffer(query)
        .map(
            bookingOffer -> {
              if (bookingOffer.getMetadata().getTicketConfigAvailable()) {
                TicketConfigResponse response =
                    adapterClient.getTicketConfigResponse(
                        offerRequest.getProviderId(), createQuery(bookingOffer, true), null);
                var updateOfferResponse =
                    toPiboxUpdateOfferResponse(response, offerRequest, bookingOffer);
                storeUpdatedOffer(bookingOffer, response, updateOfferResponse);
                return updateOfferResponse;
              }
              return PiboxUpdateOfferResponse.getDefaultInstance();
            });
  }

  private void storeUpdatedOffer(
      BookingOffer bookingOffer,
      TicketConfigResponse response,
      PiboxUpdateOfferResponse updateOfferResponse) {
    var offerStoreDocument = OfferStoreDocument.getDefaultInstance(); //
    var offerList = response.getOffers();
    /*
     need to map offerList to OfferStoreDocument and store it
     Currently BookingOffer model doesn't have ample data which can used to map to
     OfferStoreDocument
    */
    offerStore
        .putOffer(offerStoreDocument)
        .doOnSuccess(
            putBookingOfferResponse ->
                log.info(
                    "Offer was successfully store after adjusted using update offer : {}.",
                    putBookingOfferResponse))
        .doOnError(t -> log.warn("Error to store using update offer :", t))
        .subscribe();
  }

  private PiboxUpdateOfferResponse toPiboxUpdateOfferResponse(
      TicketConfigResponse response, PiboxUpdateOfferRequest request, BookingOffer bookingOffer) {
    var protobufoffers =
        protobufOfferMapper.toProtobufOffers(bookingOffer, request, response.getOffers());
    return PiboxUpdateOfferResponse.newBuilder()
        .addAllOffer(protobufoffers)
        .setStatus(PiboxUpdateOfferResponse.PiboxUpdateOfferStatus.CHANGED)
        .addActionRule(ActionRules.newBuilder().build())
        .addFare(Fare.newBuilder().build())
        .build();
  }

  private OfferDetailsQuery createOfferDetailsQuery(String provider, String offerStoreId) {
    return OfferDetailsQuery.newBuilder()
        .setProviderId(provider)
        .setOfferStoreId(offerStoreId)
        .build();
  }

  private TicketConfigRequest createQuery(BookingOffer bookingOffer, boolean isUpdateOffer) {
    var providerName = bookingOffer.getProvider();
    var travellers = bookingOffer.getOutboundSegment().getTravellersList();
    var ticketConfigRequest =
        new TicketConfigRequest()
            .setLanguage(bookingOffer.getLanguage())
            .setPassengers(
                passengerDataMapper
                    .getPassengerTypeMapper()
                    .travellersToPassengerTypesMap(providerName, travellers))
            .setPassengerData(
                passengerDataMapper.mapPassengerDataFomTravellers(providerName, travellers))
            .setCurrency(bookingOffer.getUserCurrency());
    if (isUpdateOffer) {
      return bookingOffer.getInboundSegment() != null
          ? ticketConfigRequest
              .setOffer(offerMapper.toConnectOffer(bookingOffer))
              .setOutboundSolution(solutionMapper.toSolution(bookingOffer, false))
              .setInboundSolution(solutionMapper.toSolution(bookingOffer, true))
          : ticketConfigRequest
              .setOffer(offerMapper.toConnectOffer(bookingOffer))
              .setOutboundSolution(solutionMapper.toSolution(bookingOffer, false));
    }
    return ticketConfigRequest;
  }
}
