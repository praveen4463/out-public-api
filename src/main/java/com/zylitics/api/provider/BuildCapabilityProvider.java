package com.zylitics.api.provider;

import com.zylitics.api.model.BuildCapability;

public interface BuildCapabilityProvider {
  
  void captureCapability(BuildCapability buildCapability, int buildId);
}
