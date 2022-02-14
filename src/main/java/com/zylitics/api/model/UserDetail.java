package com.zylitics.api.model;

public class UserDetail {
  
  private int userId;
  
  private int organizationId;
  
  public int getUserId() {
    return userId;
  }
  
  public UserDetail setUserId(int userId) {
    this.userId = userId;
    return this;
  }
  
  public int getOrganizationId() {
    return organizationId;
  }
  
  public UserDetail setOrganizationId(int organizationId) {
    this.organizationId = organizationId;
    return this;
  }
}
