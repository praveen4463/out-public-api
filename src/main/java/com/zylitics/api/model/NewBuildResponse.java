package com.zylitics.api.model;

public class NewBuildResponse extends AbstractBuildResponse {
  
  private int buildId;
  
  public int getBuildId() {
    return buildId;
  }
  
  public NewBuildResponse setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
}
