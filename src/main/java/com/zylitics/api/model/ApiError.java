package com.zylitics.api.model;

import javax.annotation.Nullable;

public class ApiError {
  
  @Nullable
  private Enum<?> causeType;
  
  private String message;
  
  @Nullable
  public Enum<?> getCauseType() {
    return causeType;
  }
  
  public ApiError setCauseType(Enum<?> causeType) {
    this.causeType = causeType;
    return this;
  }
  
  @SuppressWarnings("unused")
  public String getMessage() {
    return message;
  }
  
  public ApiError setMessage(String message) {
    this.message = message;
    return this;
  }
}
