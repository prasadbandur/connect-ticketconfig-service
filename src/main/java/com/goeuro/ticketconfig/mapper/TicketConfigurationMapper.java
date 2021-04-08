package com.goeuro.ticketconfig.mapper;

import com.goeuro.coverage.goeuroconnect.model.v03.TicketConfigResponse;
import com.goeuro.ticketconfig.proto.FareTerms;
import com.goeuro.ticketconfig.proto.TicketConfigurationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static java.util.Objects.nonNull;

@Component
@RequiredArgsConstructor
public class TicketConfigurationMapper {

  private final SeatSelectionInfoMapper seatSelectionInfoMapper;
  private final VehicleMapper vehicleMapper;
  private final BaggageMapper baggageMapper;

  public TicketConfigurationResponse mapResponse(
      TicketConfigResponse response, String provider, String carrier) {
    TicketConfigurationResponse.Builder builder = TicketConfigurationResponse.newBuilder();

    if (nonNull(response.getSeatSelection())) {
      builder.setSeatSelectionInfo(
          seatSelectionInfoMapper.seatSelectionToProto(
              response.getSeatSelection(), provider, carrier));
    }

    // I just found ticket fulfillment is not mapped on pi-goeuroconnect
    //    if (nonNull(response.getTicketFulfillment())) {
    //      builder.setTicketFulfillment(ticketFulfillmentMapper.mapToTicketFulfillment()));
    //    }

    if (nonNull(response.getVehicle())) {
      builder.setVehicleInfo(
          vehicleMapper.mapVehicleToProto(response.getVehicle(), provider, carrier));
    }

    if (nonNull(response.getBaggage())) {
      builder.setBaggageInfo(baggageMapper.mapBaggageToProto(response.getBaggage(), provider));
    }

    if (nonNull(response.getFareTerms())) {
      builder.setFareTerms(fareTermsToProto(response.getFareTerms()));
    }

    return builder.build();
  }

  private FareTerms fareTermsToProto(
      com.goeuro.coverage.goeuroconnect.model.v03.FareTerms fareTerms) {
    return FareTerms.newBuilder().setMessage(fareTerms.getMessage()).build();
  }
}
