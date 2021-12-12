package com.zylitics.api.provider;

import com.zylitics.api.model.IncomingFile;

import javax.annotation.Nullable;
import java.util.List;

public interface TestProvider {
  
  void captureTests(@Nullable List<IncomingFile> incomingFiles, int projectId, int buildId);
}
