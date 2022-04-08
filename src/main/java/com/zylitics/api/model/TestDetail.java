package com.zylitics.api.model;

import java.time.LocalDateTime;

public class TestDetail {
  
  private int buildId;
  
  private String file;
  
  private String test;
  
  private String version;
  
  private TestStatus status;
  
  private String error;
  
  private String urlUponError;
  
  private LocalDateTime endDate;
  
  public int getBuildId() {
    return buildId;
  }
  
  public TestDetail setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  public String getFile() {
    return file;
  }
  
  public TestDetail setFile(String file) {
    this.file = file;
    return this;
  }
  
  public String getTest() {
    return test;
  }
  
  public TestDetail setTest(String test) {
    this.test = test;
    return this;
  }
  
  public String getVersion() {
    return version;
  }
  
  public TestDetail setVersion(String version) {
    this.version = version;
    return this;
  }
  
  public TestStatus getStatus() {
    return status;
  }
  
  public TestDetail setStatus(TestStatus status) {
    this.status = status;
    return this;
  }
  
  public String getError() {
    return error;
  }
  
  public TestDetail setError(String error) {
    this.error = error;
    return this;
  }
  
  public String getUrlUponError() {
    return urlUponError;
  }
  
  public TestDetail setUrlUponError(String urlUponError) {
    this.urlUponError = urlUponError;
    return this;
  }
  
  public LocalDateTime getEndDate() {
    return endDate;
  }
  
  public TestDetail setEndDate(LocalDateTime endDate) {
    this.endDate = endDate;
    return this;
  }
}
