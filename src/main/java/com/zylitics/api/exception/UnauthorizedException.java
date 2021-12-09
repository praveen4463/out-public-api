package com.zylitics.api.exception;

public class UnauthorizedException extends RuntimeException {
  
  private static final long serialVersionUID = 6899677388603049529L;
  
  public UnauthorizedException(String message) {
    super(message);
  }
}
