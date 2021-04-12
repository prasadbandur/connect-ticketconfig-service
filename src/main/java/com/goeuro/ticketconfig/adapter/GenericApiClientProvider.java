package com.goeuro.ticketconfig.adapter;

import com.goeuro.coverage.goeuroconnect.client.AbstractApiClient;

public interface GenericApiClientProvider {
  AbstractApiClient getGenericApiForProvider(String provider);
}
