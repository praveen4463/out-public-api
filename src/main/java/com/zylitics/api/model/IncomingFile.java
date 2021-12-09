package com.zylitics.api.model;

import javax.annotation.Nullable;
import java.util.List;

public class IncomingFile {
  
  private String name;
  
  @Nullable
  private List<IncomingTest> tests;
  
  public String getName() {
    return name;
  }
  
  public IncomingFile setName(String name) {
    this.name = name;
    return this;
  }
  
  @Nullable
  public List<IncomingTest> getTests() {
    return tests;
  }
  
  public IncomingFile setTests(@Nullable List<IncomingTest> tests) {
    this.tests = tests;
    return this;
  }
}
