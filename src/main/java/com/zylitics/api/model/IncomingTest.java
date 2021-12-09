package com.zylitics.api.model;

import javax.annotation.Nullable;
import java.util.List;

public class IncomingTest {
  
  private String name;
  
  @Nullable
  private List<String> versions;
  
  public String getName() {
    return name;
  }
  
  public IncomingTest setName(String name) {
    this.name = name;
    return this;
  }
  
  @Nullable
  public List<String> getVersions() {
    return versions;
  }
  
  public IncomingTest setVersions(@Nullable List<String> versions) {
    this.versions = versions;
    return this;
  }
}
