package com.zylitics.api;

import java.io.Closeable;
import java.io.IOException;

public interface SecretsManager extends Closeable {
  
  String getSecretAsPlainText(String secretCloudFileName);
  
  @SuppressWarnings("unused")
  void reAcquireClientAfterClose() throws IOException;
}
