package com.goeuro.ticketconfig.providerconfig.wrapper;

import com.goeuro.ticketconfig.providerconfig.model.CarrierConfig;
import com.goeuro.ticketconfig.providerconfig.model.CustomAdditionalServiceConfiguration;
import com.goeuro.ticketconfig.providerconfig.model.CustomAdditionalServiceConfiguration.CustomAdditionalServiceConfigurationType;
import com.goeuro.ticketconfig.providerconfig.model.ProviderConfig;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public class CarriersConfigWrapper {

  private final ProviderConfig providerConfig;

  @VisibleForTesting
  public boolean isDisabled(String carrierCode) {
    return getCarrierConfig(providerConfig, carrierCode)
        .map(CarrierConfig::getDisabled)
        .orElse(false);
  }

  public List<String> getAllWithCustomFare() {
    return getNotEmptyCarrierConfigs(providerConfig)
        .filter(carriers -> carriers.stream().anyMatch(c -> nonNull(c.getCustomFare())))
        .map(
            carriers ->
                carriers.stream()
                    .filter(carrier -> nonNull(carrier.getCustomFare()))
                    .filter(CarrierConfig::getCustomFare)
                    .map(CarrierConfig::getName)
                    .collect(Collectors.toList()))
        .orElse(Collections.emptyList());
  }

  private static Optional<List<CarrierConfig>> getNotEmptyCarrierConfigs(
      ProviderConfig providerConfig) {
    return Optional.ofNullable(providerConfig)
        .map(ProviderConfig::getCarriers)
        .filter(ObjectUtils::isNotEmpty);
  }

  static Optional<CarrierConfig> getCarrierConfig(
      ProviderConfig providerConfig, String carrierCode) {
    return getNotEmptyCarrierConfigs(providerConfig)
        .flatMap(
            carriers ->
                carriers.stream()
                    .filter(carrier -> carrier.getName().equals(carrierCode))
                    .findFirst());
  }

  static List<CarrierConfig> getCarrierConfigs(ProviderConfig providerConfig) {
    return getNotEmptyCarrierConfigs(providerConfig).orElse(Lists.newArrayList());
  }

  public Map<String, Map<CustomAdditionalServiceConfigurationType, Boolean>>
      getAllWithCustomAdditionalServicesLabel() {
    return Optional.ofNullable(providerConfig.getCarriers()).stream()
        .flatMap(Collection::stream)
        .filter(CarriersConfigWrapper::hasCustomAdditionalServices)
        .collect(
            toMap(
                CarrierConfig::getName,
                data ->
                    data.getCustomAdditionalServices().stream()
                        .collect(
                            toMap(
                                CustomAdditionalServiceConfiguration::getType,
                                CustomAdditionalServiceConfiguration::getCustomLabel))));
  }

  private static boolean hasCustomAdditionalServices(CarrierConfig carrierConfig) {
    return Optional.ofNullable(carrierConfig.getCustomAdditionalServices()).stream()
        .flatMap(Collection::stream)
        .filter(
            customAdditionalServiceConfiguration ->
                nonNull(customAdditionalServiceConfiguration.getCustomLabel()))
        .anyMatch(CustomAdditionalServiceConfiguration::getCustomLabel);
  }

  // TODO
  //  public boolean areAllEnabled(Journey journey) {
  //    return extractCarrierCodes(journey).stream().noneMatch(this::isDisabled);
  //  }

  //  static List<String> extractCarrierCodes(Journey journey) {
  //    return journey.getLegs().stream()
  //        .flatMap(leg -> leg.getSegments().stream())
  //        .map(JourneySegment::getCarrierCode)
  //        .filter(Objects::nonNull)
  //        .distinct()
  //        .collect(Collectors.toList());
  //  }
}
