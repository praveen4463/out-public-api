package com.zylitics.api.model;

import java.util.Map;

public class BuildConfig {
  
  private String displayResolution;
  
  private String timezone;
  
  private int retryFailedTestsUpto;
  
  private Map<String, String> buildVars;
  
  public String getDisplayResolution() {
    return displayResolution;
  }
  
  public BuildConfig setDisplayResolution(String displayResolution) {
    this.displayResolution = displayResolution;
    return this;
  }
  
  public String getTimezone() {
    return timezone;
  }
  
  public BuildConfig setTimezone(String timezone) {
    this.timezone = timezone;
    return this;
  }
  
  public int getRetryFailedTestsUpto() {
    return retryFailedTestsUpto;
  }
  
  public BuildConfig setRetryFailedTestsUpto(int retryFailedTestsUpto) {
    this.retryFailedTestsUpto = retryFailedTestsUpto;
    return this;
  }
  
  public Map<String, String> getBuildVars() {
    return buildVars;
  }
  
  public BuildConfig setBuildVars(Map<String, String> buildVars) {
    this.buildVars = buildVars;
    return this;
  }
}
