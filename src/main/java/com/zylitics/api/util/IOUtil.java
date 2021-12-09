package com.zylitics.api.util;

public class IOUtil {
  
  private static final int MB_BYTES = 1048576;
  
  public static String getBytesStringRepresentation(Long bytes) {
    String size;
    if (bytes < 1024) {
      size = bytes + " byte" + (bytes > 1 ? "s" : "");
    } else if (bytes < MB_BYTES) {
      // less than 1MB, divide by KB bytes
      size = MathUtil.divideRound1(bytes, 1024) + " KB";
    } else {
      size = MathUtil.divideRound1(bytes, MB_BYTES) + " MB";
    }
    return size;
  }
}
