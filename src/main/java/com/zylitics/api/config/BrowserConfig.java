package com.zylitics.api.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class BrowserConfig {
  
  private static final List<String> FIREFOX = Arrays.asList("firefox", "ff");
  
  private static final List<String> IE = Arrays.asList("internet explorer", "ie");
  
  public static String understandBrowser(String givenBrowser) {
    Objects.requireNonNull(givenBrowser);
    
    String normalized = givenBrowser.trim().toLowerCase(Locale.US);
    if (normalized.equals("chrome")) {
      return "chrome";
    }
    if (FIREFOX.contains(normalized)) {
      return "firefox";
    }
    if (IE.contains(normalized)) {
      return "IE";
    }
    
    throw new IllegalArgumentException(givenBrowser + " is not a supported browser");
  }
}
