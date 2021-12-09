package com.zylitics.api.provider;

import com.zylitics.api.model.IncomingFile;

import java.util.List;

public interface TestProvider {
  
  void captureTests(List<IncomingFile> incomingFiles, int buildId);
}
