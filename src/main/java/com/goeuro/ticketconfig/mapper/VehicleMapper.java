package com.goeuro.ticketconfig.mapper;

import com.goeuro.coverage.goeuroconnect.model.v03.Vehicle;
import com.goeuro.search2.model.proto.Currency;
import com.goeuro.search2.model.proto.Price;
import com.goeuro.ticketconfig.mapper.TranslationKeyMapper.TranslationKeyInfo;
import com.goeuro.ticketconfig.proto.VehicleClass;
import com.goeuro.ticketconfig.proto.VehicleInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.goeuro.ticketconfig.providerconfig.model.CustomAdditionalServiceConfiguration.CustomAdditionalServiceConfigurationType.VEHICLE;
import static java.util.Objects.nonNull;

@Component
@RequiredArgsConstructor
public class VehicleMapper {

  private static final String VEHICLE_CLASS_TRANSLATION_PREFIX = "Journey.vehicle.class";
  private static final String VEHICLE_DIMENSIONS_TRANSLATION_PREFIX = "Journey.vehicle.dimensions";

  private final TranslationKeyMapper translationKeyMapper;

  public VehicleInfo mapVehicleToProto(Vehicle vehicle, String provider, String carrier) {
    TranslationKeyInfo translationKeyInfo =
        TranslationKeyInfo.builder().provider(provider).carrier(carrier).build();
    return VehicleInfo.newBuilder()
        .addAllVehicleClass(
            mapVehicleClassesToProtoList(
                vehicle.getClasses(), translationKeyInfo, vehicle.getCurrency()))
        .build();
  }

  private List<VehicleClass> mapVehicleClassesToProtoList(
      List<com.goeuro.coverage.goeuroconnect.model.v03.VehicleClass> vehicleClasses,
      TranslationKeyInfo translationKeyInfo,
      String currency) {
    return vehicleClasses.stream()
        .map(vehicleClass -> mapVehicleClassToProto(vehicleClass, translationKeyInfo, currency))
        .collect(Collectors.toList());
  }

  private VehicleClass mapVehicleClassToProto(
      com.goeuro.coverage.goeuroconnect.model.v03.VehicleClass vehicleClass,
      TranslationKeyInfo translationKeyInfo,
      String currency) {

    VehicleClass.Builder builder = createBuilder(translationKeyInfo, vehicleClass, currency);

    addDimensions(translationKeyInfo, builder);

    if (nonNull(vehicleClass.getType())) {
      builder.setType(vehicleClass.getType().name());
    }

    return builder.build();
  }

  private VehicleClass.Builder createBuilder(
      TranslationKeyInfo translationKeyInfo,
      com.goeuro.coverage.goeuroconnect.model.v03.VehicleClass vehicleClass,
      String currency) {
    translationKeyInfo.setType(VEHICLE);
    translationKeyInfo.setTranslationPrefix(VEHICLE_CLASS_TRANSLATION_PREFIX);
    return VehicleClass.newBuilder()
        .setExternalCode(vehicleClass.getExternalCode())
        .setAdditionalPrice(
            Price.newBuilder()
                .setAmountInCents(vehicleClass.getPriceInCents())
                .setCurrency(Currency.valueOf(currency))
                .build())
        .setClassLabel(translationKeyMapper.generateDynamicTranslationKey(translationKeyInfo));
  }

  private void addDimensions(TranslationKeyInfo translationKeyInfo, VehicleClass.Builder builder) {
    translationKeyInfo.setTranslationPrefix(VEHICLE_DIMENSIONS_TRANSLATION_PREFIX);
    builder.setDimensionsLabel(
        translationKeyMapper.generateDynamicTranslationKey(translationKeyInfo));
  }
}
