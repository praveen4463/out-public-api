package com.zylitics.api.provider;

import javax.annotation.Nullable;
import java.util.Map;

public interface BuildVarProvider {
  
  void capturePrimaryBuildVarsOverridingGiven(int projectId,
                                              @Nullable Map<String, String> override,
                                              int buildId);
}
