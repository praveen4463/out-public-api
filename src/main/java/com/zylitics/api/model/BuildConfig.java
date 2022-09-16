package com.zylitics.api.model;

import java.util.Map;

public class BuildConfig {
  
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
