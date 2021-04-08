package com.goeuro.ticketconfig.mapper;

import com.goeuro.coverage.goeuroconnect.model.v03.AdditionalService;
import com.goeuro.coverage.goeuroconnect.model.v03.AdditionalServiceType;
import com.goeuro.coverage.goeuroconnect.model.v03.Offer;
import com.goeuro.coverage.offer.store.protobuf.Baggage;
import com.goeuro.coverage.offer.store.protobuf.BookingOffer;
import com.goeuro.coverage.offer.store.protobuf.OfferSeatSelection;
import com.goeuro.coverage.offer.store.protobuf.Vehicle;
import com.goeuro.ticketconfig.deeplink.DeepLinkParameter;
import com.goeuro.ticketconfig.providerconfig.service.ProviderConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.goeuro.ticketconfig.deeplink.DeepLinkParameter.*;

@Component
@RequiredArgsConstructor
public class ConnectOfferMapper {

  private final ProviderConfigService providerConfigService;

  public Offer toConnectOffer(BookingOffer bookingOffer) {
    /*
     we are not mapping fare & offer parts as it is not done in pi-goeuroconnect
     we may need to update BookingOffer if we need above mapping
    */
    return new Offer()
        .setUuid(UUID.randomUUID().toString())
        .setAdditionalServices(toAdditionalServices(bookingOffer))
        .setAvailability(bookingOffer.getMetadata().getAvailability())
        .setExternalOfferReference(
            bookingOffer.getProviderParamsMap().get(PARAM_EXTERNAL_OFFER_REFERENCE))
        .setOfferId(bookingOffer.getProviderParamsMap().get(PARAM_OUTBOUND_OFFER_ID))
        .setSeatClass(bookingOffer.getProviderParamsMap().get(PARAM_OUTBOUND_SEAT_CLASS))
        .setOffsite(bookingOffer.getMetadata().getOffsite())
        .setOpenTicket(bookingOffer.getMetadata().getOpenTicket())
        .setTotalPriceInCents(bookingOffer.getProviderPrice().getValueInCents())
        .setUrl(bookingOffer.getProviderParamsMap().get(PARAM_REDIRECT_URL));
  }

  private List<AdditionalService> toAdditionalServices(BookingOffer bookingOffer) {
    var additionalServicesConfig =
        providerConfigService
            .getProviderConfigWrapper(bookingOffer.getProvider())
            .getAdditionalServices();
    if (additionalServicesConfig.isEnabled()) {
      List<AdditionalService> additionalServices = new ArrayList<>();
      Baggage baggage = bookingOffer.getBaggage();
      Vehicle vehicle = bookingOffer.getVehicle();
      OfferSeatSelection seatSelection = bookingOffer.getSeatSelection();
      if (seatSelection != null) {
        AdditionalService seatSelectionService =
            new AdditionalService()
                .setBookable(true) // we need to update BookingOffer to have data for this
                .setRequired(seatSelection.getRequired())
                .setType(AdditionalServiceType.seatSelection);
        additionalServices.add(seatSelectionService);
      }
      if (baggage != null) {
        AdditionalService baggageService =
            new AdditionalService()
                .setBookable(true) /* we need to update BookingOffer to have data for this */
                .setRequired(false) /* we need to update BookingOffer to have data for this */
                .setType(AdditionalServiceType.baggage);
        additionalServices.add(baggageService);
      }
      if (vehicle != null) {
        AdditionalService vehicleService =
            new AdditionalService()
                .setBookable(true) /* we need to update BookingOffer to have data for this */
                .setRequired(false) /* we need to update BookingOffer to have data for this */
                .setType(AdditionalServiceType.vehicle);
        additionalServices.add(vehicleService);
      }
      return additionalServices;
    }
    return Collections.emptyList();
  }
}
