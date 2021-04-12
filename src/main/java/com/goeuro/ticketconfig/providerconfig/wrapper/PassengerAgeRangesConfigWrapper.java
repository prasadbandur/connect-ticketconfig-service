package com.goeuro.ticketconfig.providerconfig.wrapper;

import com.goeuro.ticketconfig.providerconfig.model.CarrierConfig;
import com.goeuro.ticketconfig.providerconfig.model.PassengerAgeRangeConfiguration;
import com.goeuro.ticketconfig.providerconfig.model.ProviderConfig;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.goeuro.ticketconfig.providerconfig.wrapper.CarriersConfigWrapper.getCarrierConfig;
import static com.goeuro.ticketconfig.providerconfig.wrapper.CarriersConfigWrapper.getCarrierConfigs;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@RequiredArgsConstructor
public class PassengerAgeRangesConfigWrapper {

  private final ProviderConfig providerConfig;

  private List<PassengerAgeRangeConfiguration> getFromProvider() {
    return Optional.ofNullable(providerConfig)
        .filter(p -> isNotEmpty(providerConfig.getPassengerAgeRanges()))
        .map(ProviderConfig::getPassengerAgeRanges)
        .orElse(Collections.emptyList());
  }

  public List<PassengerAgeRangeConfiguration> getFromCarrier(String maybeCarrierCode) {
    return Optional.ofNullable(maybeCarrierCode)
        .flatMap(
            carrierCode ->
                getCarrierConfig(providerConfig, carrierCode)
                    .filter(carrierConfig -> isNotEmpty(carrierConfig.getPassengerAgeRanges()))
                    .map(CarrierConfig::getPassengerAgeRanges))
        .orElse(getFromProvider());
  }

  public List<String> getCarrierCodes() {
    return getCarrierConfigs(providerConfig).stream()
        .filter(carrierConfig -> isNotEmpty(carrierConfig.getPassengerAgeRanges()))
        .map(CarrierConfig::getName)
        .collect(Collectors.toList());
  }

  /**
   * Note: This rule was a decision made together with product. This function decides for only one
   * carrier code, when having multiples carrier code in the journey (in a multiple segments trip).
   * When there are multiple carrier codes in the journey, if they are all the same, we will use it
   * to get the configuration. If they are different, we will be using the general configuration of
   * the provider to solve the conflict.
   *
   * @param journey selected {@see Journey} by the user.
   * @return Optional of carrier code, as String.
   */

  // TODO
  //  public static Optional<String> resolveCarrierCode(Journey journey) {
  //    List<String> carrierCodes = extractCarrierCodes(journey);
  //    if (carrierCodes.stream().distinct().count() == 1) {
  //      return carrierCodes.stream().findFirst();
  //    }
  //    return Optional.empty();
  //  }
}
