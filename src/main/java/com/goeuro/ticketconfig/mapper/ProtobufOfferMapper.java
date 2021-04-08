package com.goeuro.ticketconfig.mapper;

import com.goeuro.coverage.goeuroconnect.model.v03.AdditionalService;
import com.goeuro.coverage.goeuroconnect.model.v03.AdditionalServiceType;
import com.goeuro.coverage.offer.store.protobuf.BookingOffer;
import com.goeuro.search2.commons.proto.UUIDUtils;
import com.goeuro.search2.model.proto.*;
import com.goeuro.search2.pi.proto.PiboxGetOfferRequest;
import com.goeuro.search2.pi.proto.PiboxUpdateOfferRequest;
import com.goeuro.ticketconfig.providerconfig.service.ProviderConfigService;
import com.google.protobuf.Int32Value;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Component
@RequiredArgsConstructor
public class ProtobufOfferMapper {

  private static final String TRANSLATION_KEY_SEPARATOR = ".";
  private final ProviderConfigService providerConfigService;

  public List<Offer> toProtobufOffers(
      BookingOffer storedBookingOffer,
      PiboxUpdateOfferRequest updateOfferRequest,
      List<com.goeuro.coverage.goeuroconnect.model.v03.Offer> offers) {
    return offers.stream()
        .map(offer -> toProtobufOffer(storedBookingOffer, updateOfferRequest, offer))
        .collect(Collectors.toList());
  }

  public Offer toProtobufOffer(BookingOffer storedBookingOffer, PiboxGetOfferRequest request) {
    /*
     we dont have ample date in above params to build Proto offer, we must change BookingOffer
     to add attributes to enable mapping
    */
    return Offer.getDefaultInstance();
  }

  private Offer toProtobufOffer(
      BookingOffer storedBookingOffer,
      PiboxUpdateOfferRequest updateOfferRequest,
      com.goeuro.coverage.goeuroconnect.model.v03.Offer offer) {
    return builderOffer(
        storedBookingOffer,
        updateOfferRequest.getProviderId(),
        updateOfferRequest.getOfferStoreId(),
        offer);
  }

  private Offer builderOffer(
      BookingOffer storedBookingOffer,
      String provider,
      String offerStoreId,
      com.goeuro.coverage.goeuroconnect.model.v03.Offer offer) {
    var translationKey =
        getTranslationKey(offer, storedBookingOffer.getOutboundSegment().getCarrierCode()); // todo
    Offer.Builder protobufOfferBuilder =
        Offer.newBuilder()
            .setId(UUIDUtils.randomProtoUUID())
            .setOfferStoreId(offerStoreId)
            .setServiceProvider(provider)
            .setPriceInCents(offer.getTotalPriceInCents())
            .setCurrency(Currency.valueOf(storedBookingOffer.getUserCurrency()))
            .setIsBookable(true)
            .setIsTicketConfigAvailable(storedBookingOffer.getMetadata().getTicketConfigAvailable())
            .setBaggageAvailable(
                toProtobufBaggageAvailable(
                    storedBookingOffer.getMetadata().getTicketConfigAvailable(),
                    getadditionalServiceMap(offer).get(AdditionalServiceType.baggage)))
            .addTicketType(TicketType.newBuilder().setId(translationKey).build())
            .setTranslationPrefix(provider)
            .setTranslationKey(translationKey)
            .setVehicleConfig(
                toProtobufVehicleConfig(
                    storedBookingOffer.getMetadata().getTicketConfigAvailable(),
                    getadditionalServiceMap(offer).get(AdditionalServiceType.vehicle)))
            .addAllAdditionalService(
                Optional.ofNullable(offer.getAdditionalServices()).stream()
                    .flatMap(Collection::stream)
                    .map(this::toProtobufAdditionalService)
                    .collect(Collectors.toList()))
            .setDeeplinkType(
                offer.isOffsite() ? Offer.DeeplinkType.OFFSITE : Offer.DeeplinkType.ONSITE)
            .setTicketFulfillmentMethod(
                resolveTicketFulfillmentMethod(storedBookingOffer.getProvider()));
    setAvailabilityAndTicketsLeft(offer, protobufOfferBuilder);
    /*
     we need more info in BookingOffer to Calculate total price of both in/out bound plus
     subOffer.
     like it is done in pi-goeuroconnect in validatePriceAndSetLegs(offer, offerBuilder, outboundOfferLeg);
    */
    return protobufOfferBuilder.build();
  }

  private void setAvailabilityAndTicketsLeft(
      com.goeuro.coverage.goeuroconnect.model.v03.Offer offer, Offer.Builder offerBuilder) {
    int ticketsLeft = offer.getAvailability();
    if (ticketsLeft > 0) {
      offerBuilder.setAvailabilityInfo(
          OfferAvailabilityInfo.newBuilder().setTotalTicketsLeft(ticketsLeft).build());
      offerBuilder.setTicketsLeft(Int32Value.newBuilder().setValue(ticketsLeft).build());
    }
  }

  private boolean toProtobufBaggageAvailable(
      boolean ticketConfigAvailable, AdditionalService additionalService) {
    return ticketConfigAvailable && nonNull(additionalService) && additionalService.isBookable();
  }

  private VehicleConfig toProtobufVehicleConfig(
      boolean ticketConfigAvailable, AdditionalService additionalService) {
    return VehicleConfig.newBuilder()
        .setBookable(
            ticketConfigAvailable && nonNull(additionalService) && additionalService.isBookable())
        .setRequired(
            ticketConfigAvailable && nonNull(additionalService) && additionalService.isRequired())
        .build();
  }

  private Map<AdditionalServiceType, AdditionalService> getadditionalServiceMap(
      com.goeuro.coverage.goeuroconnect.model.v03.Offer offer) {
    return Optional.ofNullable(offer.getAdditionalServices()).stream()
        .flatMap(Collection::stream)
        .collect(
            Collectors.toMap(AdditionalService::getType, additionalService -> additionalService));
  }

  private com.goeuro.search2.model.proto.AdditionalService toProtobufAdditionalService(
      AdditionalService additionalService) {
    return com.goeuro.search2.model.proto.AdditionalService.newBuilder()
        .setType(
            com.goeuro.search2.model.proto.AdditionalServiceType.valueOf(
                additionalService.getType().name()))
        .setBookable(additionalService.isBookable())
        .setRequired(additionalService.isRequired())
        .build();
  }

  private String getTranslationKey(
      com.goeuro.coverage.goeuroconnect.model.v03.Offer offer, String carrier) {
    StringBuilder offerId = new StringBuilder();
    Optional.ofNullable(carrier)
        .ifPresent(car -> offerId.append(car).append(TRANSLATION_KEY_SEPARATOR));
    Optional.ofNullable(offer.getSeatClass())
        .ifPresent(seatClass -> offerId.append(seatClass).append(TRANSLATION_KEY_SEPARATOR));
    offerId.append(offer.getOfferId());
    return offerId.toString();
  }

  private Offer.TicketFulfillmentMethod resolveTicketFulfillmentMethod(String provider) {
    return isMobileTicketSupported(provider)
        ? Offer.TicketFulfillmentMethod.MOBILE_TICKET
        : Offer.TicketFulfillmentMethod.UNKNOWN;
  }

  private boolean isMobileTicketSupported(String provider) {
    return providerConfigService.getProviderConfigWrapper(provider).getSearch().isMobileTicket();
  }
}
