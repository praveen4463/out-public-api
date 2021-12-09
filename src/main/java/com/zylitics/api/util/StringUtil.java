package com.zylitics.api.util;

import com.google.common.base.Strings;

public class StringUtil {
  
  /**
   * Check whether given string contains atleast one non-whitespace character
   * @param s String to check
   * @return boolean indicating whether string is blank
   */
  public static boolean isBlank(String s) {
    return s == null || s.replaceAll("\\s\\n\\t\\r", "").length() == 0;
  }
  
  public static boolean isValidEmail(String email) {
    return !Strings.isNullOrEmpty(email) && email.matches("^[^@]+@[^@]+$");
  }
}
