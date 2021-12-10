package com.zylitics.api.model;

import com.sun.org.apache.xpath.internal.operations.Bool;
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
  
  private Boolean waitForCompletion;
  
  private BuildCapability buildCapability;
  
  private BuildConfig buildConfig;
  
  @NotNull
  private List<Object> files;
  
  public static class BuildConfig {
    
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
  
  public static class BuildCapability {
    
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
  
  public Boolean getWaitForCompletion() {
    return waitForCompletion;
  }
  
  public BuildRunConfig setWaitForCompletion(Boolean waitForCompletion) {
    this.waitForCompletion = waitForCompletion;
    return this;
  }
  
  public BuildConfig getBuildConfig() {
    return buildConfig;
  }
  
  public BuildRunConfig setBuildConfig(BuildConfig buildConfig) {
    this.buildConfig = buildConfig;
    return this;
  }
  
  public BuildCapability getBuildCapability() {
    return buildCapability;
  }
  
  public BuildRunConfig setBuildCapability(BuildCapability buildCapability) {
    this.buildCapability = buildCapability;
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
