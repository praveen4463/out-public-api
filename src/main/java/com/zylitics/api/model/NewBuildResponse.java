package com.zylitics.api.model;

import java.util.List;

public class NewBuildResponse {
  
  private int buildId;
  
  private String status;
  
  private String error;
  
  private List<TestDetail> testDetails;
  
  public int getBuildId() {
    return buildId;
  }
  
  public NewBuildResponse setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  public String getStatus() {
    return status;
  }
  
  public NewBuildResponse setStatus(String status) {
    this.status = status;
    return this;
  }
  
  public String getError() {
    return error;
  }
  
  public NewBuildResponse setError(String error) {
    this.error = error;
    return this;
  }
  
  public List<TestDetail> getTestDetails() {
    return testDetails;
  }
  
  public NewBuildResponse setTestDetails(List<TestDetail> testDetails) {
    this.testDetails = testDetails;
    return this;
  }
}
