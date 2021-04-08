package com.goeuro.ticketconfig.mapper;

import com.goeuro.ticketconfig.providerconfig.model.CustomAdditionalServiceConfiguration;
import com.goeuro.ticketconfig.providerconfig.service.ProviderConfigService;
import com.goeuro.ticketconfig.providerconfig.wrapper.CarriersConfigWrapper;
import com.goeuro.ticketconfig.providerconfig.wrapper.ProviderConfigWrapper;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TranslationKeyMapper {

  private static final String TRANSLATION_KEY_SEPARATOR = ".";

  private final ProviderConfigService providerConfigService;

  public String generateStaticTranslationKey(TranslationKeyInfo translationKeyInfo) {
    return translationKeyInfo.getTranslationPrefix()
        + TRANSLATION_KEY_SEPARATOR
        + translationKeyInfo.getTranslationKeyCode();
  }

  public String generateDynamicTranslationKey(TranslationKeyInfo translationKeyInfo) {

    StringBuilder translationKey = new StringBuilder(translationKeyInfo.getProvider());

    if (isCustomAtCarrierLevel(translationKeyInfo)) {
      translationKey.append(TRANSLATION_KEY_SEPARATOR).append(translationKeyInfo.getCarrier());
    }

    return translationKey
        .append(TRANSLATION_KEY_SEPARATOR)
        .append(translationKeyInfo.getTranslationPrefix())
        .append(TRANSLATION_KEY_SEPARATOR)
        .append(translationKeyInfo.getTranslationKeyCode())
        .toString();
  }

  private boolean isCustomAtCarrierLevel(TranslationKeyInfo translationKeyInfo) {
    return Optional.ofNullable(
            providerConfigService.getProviderConfigWrapper(translationKeyInfo.getProvider()))
        .map(ProviderConfigWrapper::getCarriers)
        .map(CarriersConfigWrapper::getAllWithCustomAdditionalServicesLabel)
        .map(carriers -> carriers.get(translationKeyInfo.getCarrier()))
        .map(carrierConfig -> carrierConfig.get(translationKeyInfo.getType()))
        .orElse(false);
  }

  @Data
  @Builder
  static class TranslationKeyInfo {
    private CustomAdditionalServiceConfiguration.CustomAdditionalServiceConfigurationType type;
    private String provider;
    private String carrier;
    private String translationKeyCode;
    private String translationPrefix;
  }
}
