package com.goeuro.ticketconfig.mapper;

import com.goeuro.coverage.goeuroconnect.model.v03.DiscountCard;
import com.goeuro.coverage.goeuroconnect.model.v03.PassengerData;
import com.goeuro.coverage.offer.store.protobuf.Traveller;
import com.goeuro.search2.model.proto.Passenger;
import com.goeuro.ticketconfig.providerconfig.service.ProviderConfigService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class PassengerDataMapper {

  private final ProviderConfigService providerConfigService;

  @Getter private final PassengerTypeMapper passengerTypeMapper;

  public boolean isPassengerDataEnabled(String providerName) {
    return providerConfigService
        .getProviderConfigWrapper(providerName)
        .getSearch()
        .isPassengerData();
  }

  public List<PassengerData> mapPassengerData(String providerName, List<Passenger> passengers) {
    return mapPassengerData(providerName, null, passengers);
  }

  public List<PassengerData> mapPassengerData(
      String providerName, String carrierCode, List<Passenger> passengers) {
    return isPassengerDataEnabled(providerName)
        ? passengers.stream()
            .map(passenger -> createPassengerData(providerName, carrierCode, passenger))
            .collect(Collectors.toList())
        : Collections.emptyList();
  }

  public List<PassengerData> mapPassengerDataFomTravellers(
      String providerName, List<Traveller> travellers) {
    return isPassengerDataEnabled(providerName)
        ? travellers.stream()
            .map(traveller -> createPassengerData(providerName, traveller))
            .collect(Collectors.toList())
        : Collections.emptyList();
  }

  private PassengerData createPassengerData(
      String providerName, String carrierCode, Passenger passenger) {
    return new PassengerData()
        .setPassengerType(
            passengerTypeMapper.convertToPassengerType(
                providerName, carrierCode, passenger.getPassengerAge()))
        .setAge(passenger.getPassengerAge().getAge().getValue())
        .setDiscountCards(createDiscountCards(passenger));
  }

  private PassengerData createPassengerData(String providerName, Traveller traveller) {
    return new PassengerData()
        .setPassengerType(
            passengerTypeMapper.convertToPassengerType(
                providerName, null, traveller.getPassengerAge()))
        .setAge(traveller.getPassengerAge().getAge().getValue())
        .setDiscountCards(
            traveller.getDiscountCardsList().stream()
                .map(discountCard -> new DiscountCard().setCode(discountCard.getCode()))
                .collect(Collectors.toList()));
  }

  private List<DiscountCard> createDiscountCards(Passenger passenger) {

    return Optional.of(passenger.getDiscountCardList()).orElse(Collections.emptyList()).stream()
        .map(this::createDiscountCard)
        .collect(Collectors.toList());
  }

  private DiscountCard createDiscountCard(
      com.goeuro.search2.model.proto.DiscountCard discountCard) {
    return new DiscountCard().setCode(discountCard.getCode());
  }
}
