package com.goeuro.ticketconfig.mapper;

import com.goeuro.coverage.goeuroconnect.model.v03.SearchSegment;
import com.goeuro.coverage.goeuroconnect.model.v03.Solution;
import com.goeuro.coverage.offer.store.protobuf.BookingOffer;
import com.goeuro.coverage.offer.store.protobuf.OfferStoreSegment;
import com.goeuro.ticketconfig.deeplink.DeepLinkParameter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConnectSolutionMapper {

  public Solution toSolution(BookingOffer bookingOffer, boolean inbound) {
    var segment = inbound ? bookingOffer.getInboundSegment() : bookingOffer.getOutboundSegment();
    var solutionIdKey =
        inbound
            ? DeepLinkParameter.PARAM_INBOUND_SOLUTION_ID
            : DeepLinkParameter.PARAM_OUTBOUND_SOLUTION_ID;
    return new Solution()
        .setSolutionId(bookingOffer.getProviderParamsMap().get(solutionIdKey))
        .setArrivalDateTime(
            ZonedDateTime.of(
                LocalDateTime.parse(segment.getArrivalDateTime()),
                ZoneId.of(segment.getArrivalTimeZoneId())))
        .setArrivalStationCode(segment.getArrivalStationCode())
        .setDepartureDateTime(
            ZonedDateTime.of(
                LocalDateTime.parse(segment.getArrivalDateTime()),
                ZoneId.of(segment.getDepartureTimeZoneId())))
        .setDepartureStationCode(segment.getDepartureStationCode())
        .setMajorityCarrier(segment.getCarrierCode())
        .setSegments(
            segment.getSubSegmentsList().stream()
                .map(this::toSearchSegment)
                .collect(Collectors.toList()));
  }

  private SearchSegment toSearchSegment(OfferStoreSegment offerStoreSegment) {
    return new SearchSegment()
        .setArrivalCountryCode(offerStoreSegment.getArrivalCountryCode())
        .setArrivalDateTime(
            ZonedDateTime.of(
                LocalDateTime.parse(offerStoreSegment.getArrivalDateTime()),
                ZoneId.of(offerStoreSegment.getArrivalTimeZoneId())))
        .setArrivalStationCode(offerStoreSegment.getArrivalStationCode())
        .setCarrier(offerStoreSegment.getCarrierCode())
        .setDepartureCountryCode(offerStoreSegment.getDepartureCountryCode())
        .setDepartureDateTime(
            ZonedDateTime.of(
                LocalDateTime.parse(offerStoreSegment.getDepartureDateTime()),
                ZoneId.of(offerStoreSegment.getDepartureTimeZoneId())))
        .setDepartureStationCode(offerStoreSegment.getDepartureStationCode())
        /*
         we dont have "Line" in OfferStoreSegment/BookingOffer
        please change once model is updated
        */
        .setLine(offerStoreSegment.getAdditionalInfo())
        .setMarketingCarrier(offerStoreSegment.getCarrierCode())
        .setTravelMode(offerStoreSegment.getTravelMode());
  }
}
