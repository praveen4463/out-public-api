package com.zylitics.api.model;

public class BuildVM {
  
  private int id;
  
  private String name;
  
  private String zone;
  
  private String internalIp;
  
  private boolean deleteFromRunner;
  
  public int getId() {
    return id;
  }
  
  public BuildVM setId(int id) {
    this.id = id;
    return this;
  }
  
  public String getName() {
    return name;
  }
  
  public BuildVM setName(String name) {
    this.name = name;
    return this;
  }
  
  public String getZone() {
    return zone;
  }
  
  public BuildVM setZone(String zone) {
    this.zone = zone;
    return this;
  }
  
  public String getInternalIp() {
    return internalIp;
  }
  
  public BuildVM setInternalIp(String internalIp) {
    this.internalIp = internalIp;
    return this;
  }
  
  public boolean isDeleteFromRunner() {
    return deleteFromRunner;
  }
  
  public BuildVM setDeleteFromRunner(boolean deleteFromRunner) {
    this.deleteFromRunner = deleteFromRunner;
    return this;
  }
}
