package com.zylitics.api.exception;

import org.springframework.http.HttpStatus;

public class HttpRequestException extends RuntimeException {
  
  private static final long serialVersionUID = 2510293953657184165L;
  
  private HttpStatus status;
  
  public HttpRequestException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }
  
  public HttpStatus getStatus() {
    return status;
  }
}
