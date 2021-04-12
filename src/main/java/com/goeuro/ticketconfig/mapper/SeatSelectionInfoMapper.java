package com.goeuro.ticketconfig.mapper;

import com.goeuro.coverage.goeuroconnect.model.v03.SeatSelectionCategory;
import com.goeuro.search2.model.proto.Currency;
import com.goeuro.ticketconfig.mapper.TranslationKeyMapper.TranslationKeyInfo;
import com.goeuro.ticketconfig.proto.*;
import com.goeuro.ticketconfig.providerconfig.model.CustomAdditionalServiceConfiguration;
import com.google.protobuf.Int32Value;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.goeuro.ticketconfig.proto.SeatSelectionType.RESERVATION;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Component
@RequiredArgsConstructor
public class SeatSelectionInfoMapper {

  public static final SeatSelectionType DEFAULT_SEAT_SELECTIONS_TYPE = RESERVATION;
  private static final String SEAT_SELECTION_CATEGORY_TRANSLATION_PREFIX =
      "Journey.seatInfo.category";
  private static final String SEAT_SELECTION_OPTION_TRANSLATION_PREFIX = "Journey.seatInfo.option";

  private final TranslationKeyMapper translationKeyMapper;

  public SeatSelectionInfo seatSelectionToProto(
      com.goeuro.coverage.goeuroconnect.model.v03.SeatSelection seatSelection,
      String provider,
      String carrier) {
    TranslationKeyInfo translationKeyInfo =
        TranslationKeyInfo.builder().provider(provider).carrier(carrier).build();
    return SeatSelectionInfo.newBuilder()
        .setPriceInCents(seatSelection.getSeatReservationPriceInCents())
        .setCurrency(Currency.valueOf(seatSelection.getCurrency()))
        .setRequired(seatSelection.isRequired())
        .setSeatSelectionType(DEFAULT_SEAT_SELECTIONS_TYPE)
        .addAllSeatSelectionItems(
            seatSelectionCategoriesToProtoList(seatSelection.getCategories(), translationKeyInfo))
        .addAllIncludedInFare(includedInFaresToProtoList(seatSelection.getIncludedInFare()))
        .build();
  }

  private List<IncludedInFare> includedInFaresToProtoList(
      List<com.goeuro.coverage.goeuroconnect.model.v03.IncludedInFare> includedInFares) {
    return emptyIfNull(includedInFares).stream()
        .map(
            includedInFare ->
                IncludedInFare.newBuilder()
                    .setItemId(includedInFare.getItemId())
                    .setQuantity(includedInFare.getQuantity())
                    .build())
        .collect(Collectors.toList());
  }

  private List<SeatSelectionItem> seatSelectionCategoriesToProtoList(
      List<SeatSelectionCategory> categories, TranslationKeyInfo translationKeyInfo) {
    return emptyIfNull(categories).stream()
        .map(category -> seatSelectionCategoryToProto(category, translationKeyInfo))
        .collect(Collectors.toList());
  }

  private SeatSelectionItem seatSelectionCategoryToProto(
      SeatSelectionCategory category, TranslationKeyInfo translationKeyInfo) {
    String categoryCode = getName(category);
    translationKeyInfo.setTranslationKeyCode(categoryCode);
    translationKeyInfo.setTranslationPrefix(SEAT_SELECTION_CATEGORY_TRANSLATION_PREFIX);
    boolean providerSpecificKey = isProviderSpecificKey(category);
    return SeatSelectionItem.newBuilder()
        .setId(categoryCode)
        .setName(getSeatSelectionTranslationKey(translationKeyInfo, providerSpecificKey))
        .addAllSelectionOptions(
            seatSelectionOptionsToProtoList(
                category.getOptions(), translationKeyInfo, providerSpecificKey))
        .build();
  }

  private List<SelectionOption> seatSelectionOptionsToProtoList(
      List<com.goeuro.coverage.goeuroconnect.model.v03.SeatSelectionOption> options,
      TranslationKeyInfo translationKeyInfo,
      boolean isProviderSpecificKey) {
    return emptyIfNull(options).stream()
        .map(
            seatSelectionOption ->
                seatSelectionOptionToProto(
                    seatSelectionOption, translationKeyInfo, isProviderSpecificKey))
        .collect(Collectors.toList());
  }

  private SelectionOption seatSelectionOptionToProto(
      com.goeuro.coverage.goeuroconnect.model.v03.SeatSelectionOption option,
      TranslationKeyInfo translationKeyInfo,
      boolean isProviderSpecificKey) {

    translationKeyInfo.setTranslationPrefix(SEAT_SELECTION_OPTION_TRANSLATION_PREFIX);
    translationKeyInfo.setTranslationKeyCode(option.getId());

    SelectionOption.Builder builder =
        SelectionOption.newBuilder()
            .setTranslationKey(option.getId())
            .setValue(getSeatSelectionTranslationKey(translationKeyInfo, isProviderSpecificKey));

    Optional.ofNullable(option.getPriceInCents())
        .ifPresent(
            price -> builder.setPriceInCents(Int32Value.newBuilder().setValue(price).build()));
    Optional.ofNullable(option.getMaxQuantity()).ifPresent(builder::setMaxQuantity);
    Optional.ofNullable(option.getMaxTotalPurchasableItems())
        .ifPresent(builder::setMaxTotalPurchasableItems);
    return builder.build();
  }

  private String getName(
      SeatSelectionCategory category) {
    return nonNull(category.getExternalCode())
        ? category.getExternalCode()
        : category.getType().name();
  }

  private boolean isProviderSpecificKey(
      SeatSelectionCategory category) {
    return nonNull(category.getExternalCode()) && isNull(category.getType());
  }

  private String getSeatSelectionTranslationKey(
      TranslationKeyInfo translationKeyInfo, boolean isProviderSpecificKey) {
    translationKeyInfo.setType(
        CustomAdditionalServiceConfiguration.CustomAdditionalServiceConfigurationType
            .SEAT_SELECTION);
    return isProviderSpecificKey
        ? translationKeyMapper.generateDynamicTranslationKey(translationKeyInfo)
        : translationKeyMapper.generateStaticTranslationKey(translationKeyInfo);
  }
}
