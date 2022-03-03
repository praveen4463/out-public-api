package com.zylitics.api.model;

import java.util.List;

public class NewBuild {
  
  private String buildName;
  
  private BuildSourceType sourceType;
  
  private String shotBucket;
  
  private BuildConfig buildConfig;
  
  private BuildCapability buildCapability;
  
  private List<IncomingFile> files;
  
  private long buildRequestId;
  
  public String getBuildName() {
    return buildName;
  }
  
  public NewBuild setBuildName(String buildName) {
    this.buildName = buildName;
    return this;
  }
  
  public BuildSourceType getSourceType() {
    return sourceType;
  }
  
  public NewBuild setSourceType(BuildSourceType sourceType) {
    this.sourceType = sourceType;
    return this;
  }
  
  public String getShotBucket() {
    return shotBucket;
  }
  
  public NewBuild setShotBucket(String shotBucket) {
    this.shotBucket = shotBucket;
    return this;
  }
  
  public BuildConfig getBuildConfig() {
    return buildConfig;
  }
  
  public NewBuild setBuildConfig(BuildConfig buildConfig) {
    this.buildConfig = buildConfig;
    return this;
  }
  
  public BuildCapability getBuildCapability() {
    return buildCapability;
  }
  
  public NewBuild setBuildCapability(BuildCapability buildCapability) {
    this.buildCapability = buildCapability;
    return this;
  }
  
  public List<IncomingFile> getFiles() {
    return files;
  }
  
  public NewBuild setFiles(List<IncomingFile> files) {
    this.files = files;
    return this;
  }
  
  public long getBuildRequestId() {
    return buildRequestId;
  }
  
  public NewBuild setBuildRequestId(long buildRequestId) {
    this.buildRequestId = buildRequestId;
    return this;
  }
}
