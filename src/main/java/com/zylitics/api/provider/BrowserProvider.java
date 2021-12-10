package com.zylitics.api.provider;

import javax.annotation.Nullable;

public interface BrowserProvider {
  
  String getLaterBrowsersVersion(String browserName);
  
  boolean browserExists(String browserName, String browserVersion);
}
