package com.zylitics.api.provider;

import java.util.Map;

public interface BuildVarProvider {
  
  void capturePrimaryBuildVarsOverridingGiven(int projectId,
                                              Map<String, String> override,
                                              int buildId);
}
