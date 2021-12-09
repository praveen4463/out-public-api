package com.zylitics.api.model;

public class NewBuildVM {
  
  private int buildId;
  
  private boolean requireRunningVM;

  private String os;
  
  private String browserName;
  
  private String browserVersion;
  
  private String displayResolution;
  
  private String timezone;
  
  public int getBuildId() {
    return buildId;
  }
  
  public NewBuildVM setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  public boolean isRequireRunningVM() {
    return requireRunningVM;
  }
  
  public NewBuildVM setRequireRunningVM(boolean requireRunningVM) {
    this.requireRunningVM = requireRunningVM;
    return this;
  }
  
  public String getOs() {
    return os;
  }
  
  public NewBuildVM setOs(String os) {
    this.os = os;
    return this;
  }
  
  public String getBrowserName() {
    return browserName;
  }
  
  public NewBuildVM setBrowserName(String browserName) {
    this.browserName = browserName;
    return this;
  }
  
  public String getBrowserVersion() {
    return browserVersion;
  }
  
  public NewBuildVM setBrowserVersion(String browserVersion) {
    this.browserVersion = browserVersion;
    return this;
  }
  
  public String getDisplayResolution() {
    return displayResolution;
  }
  
  public NewBuildVM setDisplayResolution(String displayResolution) {
    this.displayResolution = displayResolution;
    return this;
  }
  
  public String getTimezone() {
    return timezone;
  }
  
  public NewBuildVM setTimezone(String timezone) {
    this.timezone = timezone;
    return this;
  }
}
