package com.zylitics.api.util;

public class OSDescriptor {
  
  private String name;
  
  private String platform;
  
  public String getName() {
    return name;
  }
  
  public OSDescriptor setName(String name) {
    this.name = name;
    return this;
  }
  
  public String getPlatform() {
    return platform;
  }
  
  public OSDescriptor setPlatform(String platform) {
    this.platform = platform;
    return this;
  }
}
