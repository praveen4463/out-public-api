package com.zylitics.api.provider;

public interface BrowserProvider {
  
  String getLaterBrowsersVersion(String browserName);
  
  boolean browserExists(String browserName, String browserVersion);
}
