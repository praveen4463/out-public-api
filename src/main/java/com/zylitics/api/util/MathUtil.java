package com.zylitics.api.util;

import java.text.DecimalFormat;

public class MathUtil {
  
  public static String formatDecimalRound1(double value) {
    return new DecimalFormat("#.#").format(value);
  }
  
  public static String formatDecimalRound2(double value) {
    return new DecimalFormat("#.##").format(value);
  }
  
  // https://stackoverflow.com/questions/4685450/int-division-why-is-the-result-of-1-3-0
  // if both sides of divisions are integer, we need one with float or double to get number with decimal
  // points else result is truncated.
  public static String divideRound1(long dividend, long divisor) {
    return formatDecimalRound1((double)dividend / divisor);
  }
  
  public static String divideRound2(long dividend, long divisor) {
    return formatDecimalRound2((double)dividend / divisor);
  }
}
