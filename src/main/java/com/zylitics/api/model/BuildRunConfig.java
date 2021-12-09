package com.zylitics.api.model;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Validated
public class BuildRunConfig {
  
  private String buildName;
  
  private boolean waitForCompletion;
  
  private BuildCapability buildCapability;
  
  private String displayResolution;
  
  private String timezone;
  
  private int retryFailedTestsUpto;
  
  private Map<String, String> buildVars;
  
  @NotNull
  private List<Object> files;
  
  private static class BuildCapability {
    
    private String os;
    
    private String browser;
    
    private String browserVersion;
  
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
  }
  
  public String getBuildName() {
    return buildName;
  }
  
  public BuildRunConfig setBuildName(String buildName) {
    this.buildName = buildName;
    return this;
  }
  
  public boolean isWaitForCompletion() {
    return waitForCompletion;
  }
  
  public BuildRunConfig setWaitForCompletion(boolean waitForCompletion) {
    this.waitForCompletion = waitForCompletion;
    return this;
  }
  
  public BuildCapability getBuildCapability() {
    return buildCapability;
  }
  
  public BuildRunConfig setBuildCapability(BuildCapability buildCapability) {
    this.buildCapability = buildCapability;
    return this;
  }
  
  public String getDisplayResolution() {
    return displayResolution;
  }
  
  public BuildRunConfig setDisplayResolution(String displayResolution) {
    this.displayResolution = displayResolution;
    return this;
  }
  
  public String getTimezone() {
    return timezone;
  }
  
  public BuildRunConfig setTimezone(String timezone) {
    this.timezone = timezone;
    return this;
  }
  
  public int getRetryFailedTestsUpto() {
    return retryFailedTestsUpto;
  }
  
  public BuildRunConfig setRetryFailedTestsUpto(int retryFailedTestsUpto) {
    this.retryFailedTestsUpto = retryFailedTestsUpto;
    return this;
  }
  
  public Map<String, String> getBuildVars() {
    return buildVars;
  }
  
  public BuildRunConfig setBuildVars(Map<String, String> buildVars) {
    this.buildVars = buildVars;
    return this;
  }
  
  public List<Object> getFiles() {
    return files;
  }
  
  public BuildRunConfig setFiles(List<Object> files) {
    this.files = files;
    return this;
  }
}
