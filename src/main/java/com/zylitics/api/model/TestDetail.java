package com.zylitics.api.model;

public class TestDetail {
  
  private String file;
  
  private String test;
  
  private String version;
  
  private TestStatus status;
  
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
}
