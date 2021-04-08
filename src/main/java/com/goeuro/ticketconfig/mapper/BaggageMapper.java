package com.goeuro.ticketconfig.mapper;

import com.goeuro.coverage.goeuroconnect.model.v03.Baggage;
import com.goeuro.search2.model.proto.Currency;
import com.goeuro.search2.model.proto.Price;
import com.goeuro.ticketconfig.proto.*;
import com.goeuro.ticketconfig.providerconfig.model.BaggageItemConfig;
import com.goeuro.ticketconfig.providerconfig.model.DimensionConfig;
import com.goeuro.ticketconfig.providerconfig.model.WeightConfig;
import com.goeuro.ticketconfig.providerconfig.service.ProviderConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BaggageMapper {
  private static final String DEFAULT_SIZE_UNIT = "CM";
  private static final String DEFAULT_WEIGHT_UNIT = "KG";

  private final ProviderConfigService providerConfigService;

  public BaggageInfo mapBaggageToProto(Baggage baggage, String provider) {

    return BaggageInfo.newBuilder()
        .addAllItems(mapBaggageItemsToProtoList(baggage.getItems(), provider))
        .addAllIncludedInFare(mapIncludeInFareToProtoList(baggage.getIncludedInFare()))
        .addAllAvailableForPurchasing(
            mapAvailableForPurchasingToProtoList(baggage.getAvailableForPurchasing()))
        .build();
  }

  private List<BaggageItem> mapBaggageItemsToProtoList(
      List<com.goeuro.coverage.goeuroconnect.model.v03.BaggageItem> items, String provider) {

    return items.stream()
        .map(item -> this.mapBaggageItemToProto(item, provider))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  private Optional<BaggageItem> mapBaggageItemToProto(
      com.goeuro.coverage.goeuroconnect.model.v03.BaggageItem item, String provider) {
    BaggageItemConfig baggageItemConfig = getBaggageItemConfig(provider, item.getId());
    Optional<Dimensions> dimensions = dimensionToProto(baggageItemConfig.getMaxDimension());
    Optional<Weight> weight = weightToProto(baggageItemConfig.getMaxWeight());

    return Optional.of(
            BaggageItem.newBuilder()
                .setId(item.getId())
                .setType(
                    Optional.ofNullable(baggageItemConfig.getType()).map(Enum::name).orElse("")))
        .map(
            builder ->
                dimensions.isPresent() && weight.isPresent()
                    ? builder.setMaxDimensions(dimensions.get()).setMaxWeight(weight.get()).build()
                    : null);
  }

  private List<IncludedInFare> mapIncludeInFareToProtoList(
      List<com.goeuro.coverage.goeuroconnect.model.v03.IncludedInFare> includedInFareList) {
    return includedInFareList.stream()
        .map(this::mapIncludeInFareToProto)
        .collect(Collectors.toList());
  }

  private IncludedInFare mapIncludeInFareToProto(
      com.goeuro.coverage.goeuroconnect.model.v03.IncludedInFare includedInFare) {
    return IncludedInFare.newBuilder()
        .setItemId(includedInFare.getItemId())
        .setQuantity(includedInFare.getQuantity())
        .build();
  }

  private List<PurchasableItem> mapAvailableForPurchasingToProtoList(
      List<com.goeuro.coverage.goeuroconnect.model.v03.PurchasableItem> purchasableItems) {
    return purchasableItems.stream()
        .map(this::mapAvailableForPurchasingToProto)
        .collect(Collectors.toList());
  }

  private PurchasableItem mapAvailableForPurchasingToProto(
      com.goeuro.coverage.goeuroconnect.model.v03.PurchasableItem item) {
    return PurchasableItem.newBuilder()
        .setItemId(item.getItemId())
        .setUnitPrice(
            Price.newBuilder()
                .setCurrency(Currency.valueOf(item.getCurrency()))
                .setAmountInCents(item.getUnitPrice())
                .build())
        .setMaxQuantity(item.getMaxQuantity())
        .build();
  }

  private Optional<Dimensions> dimensionToProto(DimensionConfig dimensionConfig) {
    return dimensionConfig != null
        ? Optional.of(
            Dimensions.newBuilder()
                .setHeight(dimensionConfig.getHeight())
                .setWidth(dimensionConfig.getWidth())
                .setLength(dimensionConfig.getLength())
                .setMeasurementUnit(DEFAULT_SIZE_UNIT)
                .build())
        : Optional.empty();
  }

  private Optional<Weight> weightToProto(WeightConfig weightConfig) {
    return weightConfig != null
        ? Optional.of(
            Weight.newBuilder()
                .setValue(weightConfig.getWeight())
                .setMeasurementUnit(DEFAULT_WEIGHT_UNIT)
                .build())
        : Optional.empty();
  }

  private BaggageItemConfig getBaggageItemConfig(String provider, String id) {
    return providerConfigService
        .getProviderConfigWrapper(provider)
        .getAdditionalServices()
        .getBaggageItems()
        .stream()
        .filter(baggageItemConfig -> baggageItemConfig.getId().equals(id))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    String.format(
                        "Baggage id %s is not configured in CMS for provider %s", id, provider)));
  }
}
