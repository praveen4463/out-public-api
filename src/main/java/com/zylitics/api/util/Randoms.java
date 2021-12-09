package com.zylitics.api.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class Randoms {
  
  private final static String CHAR_SET =
      "0123456789abcdefghizklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
  
  private final static int CHAR_SET_LENGTH = 62;
  
  private final Random random = new Random();
  
  public String generateRandom(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(CHAR_SET.charAt(random.nextInt(CHAR_SET_LENGTH)));
    }
    return sb.toString();
  }
}