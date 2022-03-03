package com.zylitics.api.model;

public class BuildRequest {
  
  private BuildSourceType buildSourceType;
  
  private int totalParallel;
  
  private int userId;
  
  public BuildSourceType getBuildSourceType() {
    return buildSourceType;
  }
  
  public BuildRequest setBuildSourceType(BuildSourceType buildSourceType) {
    this.buildSourceType = buildSourceType;
    return this;
  }
  
  public int getTotalParallel() {
    return totalParallel;
  }
  
  public BuildRequest setTotalParallel(int totalParallel) {
    this.totalParallel = totalParallel;
    return this;
  }
  
  public int getUserId() {
    return userId;
  }
  
  public BuildRequest setUserId(int userId) {
    this.userId = userId;
    return this;
  }
}
