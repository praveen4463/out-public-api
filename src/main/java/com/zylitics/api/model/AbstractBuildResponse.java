package com.zylitics.api.model;

import java.util.List;

public abstract class AbstractBuildResponse {
  
  private String status;
  
  private String error;
  
  private List<TestDetail> testDetails;
  
  public String getStatus() {
    return status;
  }
  
  public AbstractBuildResponse setStatus(String status) {
    this.status = status;
    return this;
  }
  
  public String getError() {
    return error;
  }
  
  public AbstractBuildResponse setError(String error) {
    this.error = error;
    return this;
  }
  
  public List<TestDetail> getTestDetails() {
    return testDetails;
  }
  
  public AbstractBuildResponse setTestDetails(List<TestDetail> testDetails) {
    this.testDetails = testDetails;
    return this;
  }
}
