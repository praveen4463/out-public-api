package com.zylitics.api.provider;

import com.zylitics.api.model.APIDefaults;

import java.util.Optional;

public interface APIDefaultsProvider {
  
  Optional<APIDefaults> getApiDefaults(int organizationId);
}
