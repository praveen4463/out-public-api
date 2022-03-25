package com.zylitics.api.config;

import com.zylitics.api.model.OSDescriptor;

import java.util.*;

public class OSConfig {
  
  private static final List<String> WIN10 = Arrays.asList("windows10", "win10", "windows-10");
  
  private static final List<String> WIN8_1 = Arrays.asList("windows8.1", "win8.1", "windows-8.1");
  
  public static OSDescriptor understandOs(String givenOS) {
    Objects.requireNonNull(givenOS);
    
    String normalized = givenOS.trim().toLowerCase(Locale.US);
    if (WIN10.contains(normalized)) {
      return new OSDescriptor().setName("win10").setPlatform("windows");
    }
    if (WIN8_1.contains(normalized)) {
      return new OSDescriptor().setName("win8_1").setPlatform("windows");
    }
    
    throw new IllegalArgumentException(givenOS + " is not a supported OS");
  }
}
