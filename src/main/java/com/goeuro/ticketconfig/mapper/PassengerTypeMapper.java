package com.goeuro.ticketconfig.mapper;

import com.goeuro.coverage.goeuroconnect.model.v03.PassengerType;
import com.goeuro.coverage.offer.store.protobuf.Traveller;
import com.goeuro.search2.model.proto.Passenger;
import com.goeuro.search2.model.proto.PassengerAge;
import com.goeuro.ticketconfig.providerconfig.model.PassengerAgeRangeConfiguration;
import com.goeuro.ticketconfig.providerconfig.service.ProviderConfigService;
import com.google.common.collect.Range;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class PassengerTypeMapper {

  private final ProviderConfigService providerConfigService;

  public Map<PassengerType, Integer> toPassengerTypesMap(
      String providerName, List<Passenger> passengers) {
    Map<PassengerType, Integer> passengerTypesCounters =
        passengers.stream()
            .map(
                passenger ->
                    convertToPassengerType(providerName, null, passenger.getPassengerAge()))
            .collect(Collectors.toMap(s -> s, s -> 1, Integer::sum))
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey(Comparator.comparing(Enum::name)))
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (oldValue, newValue) -> oldValue,
                    LinkedHashMap::new));
    Stream.of(PassengerType.values()).forEach(type -> passengerTypesCounters.putIfAbsent(type, 0));
    return passengerTypesCounters;
  }

  public Map<PassengerType, Integer> travellersToPassengerTypesMap(
      String providerName, List<Traveller> travellers) {

    Map<PassengerType, Integer> passengerTypesCounters =
        travellers.stream()
            .map(traveler -> convertToPassengerType(providerName, null, traveler.getPassengerAge()))
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, value -> value.getValue().intValue()));
    Stream.of(PassengerType.values()).forEach(type -> passengerTypesCounters.putIfAbsent(type, 0));
    return passengerTypesCounters;
  }

  public PassengerType convertToPassengerType(
      String providerName, String carrier, PassengerAge passengerAge) {
    var manuallyEnteredAge = passengerAge.getAge();
    return manuallyEnteredAge.getValue() != 0
        ? toPassengerType(providerName, carrier, manuallyEnteredAge.getValue())
        : toPassengerType(providerName, carrier, passengerAge);
  }

  private PassengerType toPassengerType(String providerName, String carrierCode, Integer age) {
    return toPassengerType(
        providerConfigService
            .getProviderConfigWrapper(providerName)
            .getPassengerAgeRanges()
            .getFromCarrier(carrierCode),
        age);
  }

  private PassengerType toPassengerType(
          List<PassengerAgeRangeConfiguration> passengerAgeRanges, Integer age) {
    return passengerAgeRanges.stream()
        .filter(
            providerAgeRange ->
                age >= providerAgeRange.getMin() && age <= providerAgeRange.getMax())
        .findFirst()
        .map(PassengerAgeRangeConfiguration::getType)
        .orElse(PassengerType.adult);
  }

  private PassengerType toPassengerType(
      String providerName, String carrierCode, PassengerAge passengerAge) {
    Range<Integer> selectedAgeRange = Range.closed(passengerAge.getMin(), passengerAge.getMax());
    return providerConfigService
        .getProviderConfigWrapper(providerName)
        .getPassengerAgeRanges()
        .getFromCarrier(carrierCode)
        .stream()
        .sorted(Comparator.comparing(PassengerAgeRangeConfiguration::getMax).reversed())
        .filter(
            range -> {
              Range<Integer> providerAgeRange = Range.closed(range.getMin(), range.getMax());
              return providerAgeRange.encloses(selectedAgeRange)
                  || (providerAgeRange.isConnected(selectedAgeRange)
                      && !providerAgeRange.intersection(selectedAgeRange).isEmpty());
            })
        .findFirst()
        .map(PassengerAgeRangeConfiguration::getType)
        .orElse(PassengerType.adult);
  }
}
