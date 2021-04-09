package com.goeuro.ticketconfig.service;

import com.goeuro.coverage.goeuroconnect.model.v03.TicketConfigRequest;
import com.goeuro.coverage.goeuroconnect.model.v03.TicketConfigResponse;
import com.goeuro.coverage.offer.store.protobuf.BookingOffer;
import com.goeuro.search2.pi.proto.OfferDetailsQuery;
import com.goeuro.search2.pi.proto.TicketConfigurationRequest;
import com.goeuro.ticketconfig.adapter.AdapterClient;
import com.goeuro.ticketconfig.mapper.ConnectOfferMapper;
import com.goeuro.ticketconfig.mapper.ConnectSolutionMapper;
import com.goeuro.ticketconfig.mapper.PassengerDataMapper;
import com.goeuro.ticketconfig.mapper.TicketConfigurationMapper;
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

  private OfferDetailsQuery createOfferDetailsQuery(String provider, String offerStoreId) {
    return OfferDetailsQuery.newBuilder()
        .setProviderId(provider)
        .setOfferStoreId(offerStoreId)
        .build();
  }

  //FIXME: No need to pass @Param isUpdateOffer as this service is meant only for getTicketConfig
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
