package com.zylitics.api.model;

public class Build {
  
  private int buildId;
  
  private String buildKey;
  
  private String name;
  
  private Integer buildVMId;
  
  private String serverScreenSize;
  
  private String serverTimezone;
  
  private String sessionKey;
  
  private Long sessionRequestStartDate;
  
  private Long sessionRequestEndDate;
  
  private SessionFailureReason sessionFailureReason;
  
  private Long startDate;
  
  private Long endDate;
  
  private Long allDoneDate;
  
  private TestStatus finalStatus;
  
  private String error;
  
  private String shotBucketSessionStorage;
  
  private boolean abortOnFailure;
  
  private boolean aetKeepSingleWindow;
  
  private boolean aetUpdateUrlBlank;
  
  private boolean aetResetTimeouts;
  
  private boolean aetDeleteAllCookies;
  
  private int projectId;
  
  private int organizationId;
  
  private BuildSourceType sourceType;
  
  private long buildRequestId;
  
  private long createDate;
  
  public int getBuildId() {
    return buildId;
  }
  
  public Build setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  public String getBuildKey() {
    return buildKey;
  }
  
  public Build setBuildKey(String buildKey) {
    this.buildKey = buildKey;
    return this;
  }
  
  public String getName() {
    return name;
  }
  
  public Build setName(String name) {
    this.name = name;
    return this;
  }
  
  public Integer getBuildVMId() {
    return buildVMId;
  }
  
  public Build setBuildVMId(Integer buildVMId) {
    this.buildVMId = buildVMId;
    return this;
  }
  
  public String getServerScreenSize() {
    return serverScreenSize;
  }
  
  public Build setServerScreenSize(String serverScreenSize) {
    this.serverScreenSize = serverScreenSize;
    return this;
  }
  
  public String getServerTimezone() {
    return serverTimezone;
  }
  
  public Build setServerTimezone(String serverTimezone) {
    this.serverTimezone = serverTimezone;
    return this;
  }
  
  public String getSessionKey() {
    return sessionKey;
  }
  
  public Build setSessionKey(String sessionKey) {
    this.sessionKey = sessionKey;
    return this;
  }
  
  public Long getSessionRequestStartDate() {
    return sessionRequestStartDate;
  }
  
  public Build setSessionRequestStartDate(Long sessionRequestStartDate) {
    this.sessionRequestStartDate = sessionRequestStartDate;
    return this;
  }
  
  public Long getSessionRequestEndDate() {
    return sessionRequestEndDate;
  }
  
  public Build setSessionRequestEndDate(Long sessionRequestEndDate) {
    this.sessionRequestEndDate = sessionRequestEndDate;
    return this;
  }
  
  public SessionFailureReason getSessionFailureReason() {
    return sessionFailureReason;
  }
  
  public Build setSessionFailureReason(SessionFailureReason sessionFailureReason) {
    this.sessionFailureReason = sessionFailureReason;
    return this;
  }
  
  public Long getStartDate() {
    return startDate;
  }
  
  public Build setStartDate(Long startDate) {
    this.startDate = startDate;
    return this;
  }
  
  public Long getEndDate() {
    return endDate;
  }
  
  public Build setEndDate(Long endDate) {
    this.endDate = endDate;
    return this;
  }
  
  public Long getAllDoneDate() {
    return allDoneDate;
  }
  
  public Build setAllDoneDate(Long allDoneDate) {
    this.allDoneDate = allDoneDate;
    return this;
  }
  
  public TestStatus getFinalStatus() {
    return finalStatus;
  }
  
  public Build setFinalStatus(TestStatus finalStatus) {
    this.finalStatus = finalStatus;
    return this;
  }
  
  public String getError() {
    return error;
  }
  
  public Build setError(String error) {
    this.error = error;
    return this;
  }
  
  public String getShotBucketSessionStorage() {
    return shotBucketSessionStorage;
  }
  
  public Build setShotBucketSessionStorage(String shotBucketSessionStorage) {
    this.shotBucketSessionStorage = shotBucketSessionStorage;
    return this;
  }
  
  public boolean isAbortOnFailure() {
    return abortOnFailure;
  }
  
  public Build setAbortOnFailure(boolean abortOnFailure) {
    this.abortOnFailure = abortOnFailure;
    return this;
  }
  
  public boolean isAetKeepSingleWindow() {
    return aetKeepSingleWindow;
  }
  
  public Build setAetKeepSingleWindow(boolean aetKeepSingleWindow) {
    this.aetKeepSingleWindow = aetKeepSingleWindow;
    return this;
  }
  
  public boolean isAetUpdateUrlBlank() {
    return aetUpdateUrlBlank;
  }
  
  public Build setAetUpdateUrlBlank(boolean aetUpdateUrlBlank) {
    this.aetUpdateUrlBlank = aetUpdateUrlBlank;
    return this;
  }
  
  public boolean isAetResetTimeouts() {
    return aetResetTimeouts;
  }
  
  public Build setAetResetTimeouts(boolean aetResetTimeouts) {
    this.aetResetTimeouts = aetResetTimeouts;
    return this;
  }
  
  public boolean isAetDeleteAllCookies() {
    return aetDeleteAllCookies;
  }
  
  public Build setAetDeleteAllCookies(boolean aetDeleteAllCookies) {
    this.aetDeleteAllCookies = aetDeleteAllCookies;
    return this;
  }
  
  public int getProjectId() {
    return projectId;
  }
  
  public Build setProjectId(int projectId) {
    this.projectId = projectId;
    return this;
  }
  
  public int getOrganizationId() {
    return organizationId;
  }
  
  public Build setOrganizationId(int organizationId) {
    this.organizationId = organizationId;
    return this;
  }
  
  public BuildSourceType getSourceType() {
    return sourceType;
  }
  
  public Build setSourceType(BuildSourceType sourceType) {
    this.sourceType = sourceType;
    return this;
  }
  
  public long getBuildRequestId() {
    return buildRequestId;
  }
  
  public Build setBuildRequestId(long buildRequestId) {
    this.buildRequestId = buildRequestId;
    return this;
  }
  
  public long getCreateDate() {
    return createDate;
  }
  
  public Build setCreateDate(long createDate) {
    this.createDate = createDate;
    return this;
  }
}
