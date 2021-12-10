package com.zylitics.api.model;

public class BuildCapability {
  
  private String os;
  
  private String browser;
  
  private String browserVersion;
  
  private String platform;
  
  public String getOs() {
    return os;
  }
  
  public BuildCapability setOs(String os) {
    this.os = os;
    return this;
  }
  
  public String getBrowser() {
    return browser;
  }
  
  public BuildCapability setBrowser(String browser) {
    this.browser = browser;
    return this;
  }
  
  public String getBrowserVersion() {
    return browserVersion;
  }
  
  public BuildCapability setBrowserVersion(String browserVersion) {
    this.browserVersion = browserVersion;
    return this;
  }
  
  public String getPlatform() {
    return platform;
  }
  
  public BuildCapability setPlatform(String platform) {
    this.platform = platform;
    return this;
  }
}
