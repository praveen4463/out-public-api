package com.zylitics.api.model;

public class APIDefaults {
  
  private Integer retryFailedTestsUpto;
  
  public Integer getRetryFailedTestsUpto() {
    return retryFailedTestsUpto;
  }
  
  public APIDefaults setRetryFailedTestsUpto(Integer retryFailedTestsUpto) {
    this.retryFailedTestsUpto = retryFailedTestsUpto;
    return this;
  }
}
