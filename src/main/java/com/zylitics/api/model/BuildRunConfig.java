package com.zylitics.api.model;

import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Validated
public class BuildRunConfig {
  
  private String buildName;
  
  private Boolean waitForCompletion;
  
  private boolean includeDetailedResultInResponse;
  
  private BuildCapability buildCapability;
  
  private BuildConfig buildConfig;
  
  private List<IncomingFile> files;
  
  public static class BuildConfig {
    
    private int totalParallel;
    
    private String displayResolution;
  
    private String timezone;
  
    private Boolean captureShots;
  
    private boolean captureDriverLogs;
  
    private Integer retryFailedTestsUpto;
  
    private boolean notifyOnCompletion;
  
    private Map<String, String> buildVars;
    
    private boolean deleteAllCookiesAfterEachTest;
  
    private boolean updateUrlBlankAfterEachTest;
  
    private boolean resetTimeoutsAfterEachTest;
  
    public int getTotalParallel() {
      return totalParallel;
    }
  
    public BuildConfig setTotalParallel(int totalParallel) {
      this.totalParallel = totalParallel;
      return this;
    }
  
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
  
    public Boolean isCaptureShots() {
      return captureShots;
    }
  
    public BuildConfig setCaptureShots(Boolean captureShots) {
      this.captureShots = captureShots;
      return this;
    }
  
    public boolean isCaptureDriverLogs() {
      return captureDriverLogs;
    }
  
    public BuildConfig setCaptureDriverLogs(boolean captureDriverLogs) {
      this.captureDriverLogs = captureDriverLogs;
      return this;
    }
  
    public Integer getRetryFailedTestsUpto() {
      return retryFailedTestsUpto;
    }
  
    public BuildConfig setRetryFailedTestsUpto(Integer retryFailedTestsUpto) {
      this.retryFailedTestsUpto = retryFailedTestsUpto;
      return this;
    }
  
    public boolean getNotifyOnCompletion() {
      return notifyOnCompletion;
    }
  
    public BuildConfig setNotifyOnCompletion(boolean notifyOnCompletion) {
      this.notifyOnCompletion = notifyOnCompletion;
      return this;
    }
  
    public Map<String, String> getBuildVars() {
      return buildVars;
    }
  
    public BuildConfig setBuildVars(Map<String, String> buildVars) {
      this.buildVars = buildVars;
      return this;
    }
  
    public boolean isDeleteAllCookiesAfterEachTest() {
      return deleteAllCookiesAfterEachTest;
    }
  
    public BuildConfig setDeleteAllCookiesAfterEachTest(boolean deleteAllCookiesAfterEachTest) {
      this.deleteAllCookiesAfterEachTest = deleteAllCookiesAfterEachTest;
      return this;
    }
  
    public boolean isUpdateUrlBlankAfterEachTest() {
      return updateUrlBlankAfterEachTest;
    }
  
    public BuildConfig setUpdateUrlBlankAfterEachTest(boolean updateUrlBlankAfterEachTest) {
      this.updateUrlBlankAfterEachTest = updateUrlBlankAfterEachTest;
      return this;
    }
  
    public boolean isResetTimeoutsAfterEachTest() {
      return resetTimeoutsAfterEachTest;
    }
  
    public BuildConfig setResetTimeoutsAfterEachTest(boolean resetTimeoutsAfterEachTest) {
      this.resetTimeoutsAfterEachTest = resetTimeoutsAfterEachTest;
      return this;
    }
  }
  
  public static class BuildCapability {
    
    private String os;
    
    private String browser;
    
    private String browserVersion;
    
    private String meDeviceType;
  
    private String meDeviceDimensions;
  
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
  
    public String getMeDeviceType() {
      return meDeviceType;
    }
  
    public BuildCapability setMeDeviceType(String meDeviceType) {
      this.meDeviceType = meDeviceType;
      return this;
    }
  
    public String getMeDeviceDimensions() {
      return meDeviceDimensions;
    }
  
    public BuildCapability setMeDeviceDimensions(String meDeviceDimensions) {
      this.meDeviceDimensions = meDeviceDimensions;
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
  
  public boolean isIncludeDetailedResultInResponse() {
    return includeDetailedResultInResponse;
  }
  
  public BuildRunConfig setIncludeDetailedResultInResponse(boolean includeDetailedResultInResponse) {
    this.includeDetailedResultInResponse = includeDetailedResultInResponse;
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
  
  public List<IncomingFile> getFiles() {
    return files;
  }
  
  public BuildRunConfig setFiles(List<IncomingFile> files) {
    this.files = files;
    return this;
  }
}
