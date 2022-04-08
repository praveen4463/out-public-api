package com.zylitics.api.util;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class CommonUtil {
  
  public static void validateSingleRowDbCommit(int result) {
    if (result != 1) {
      throw new RuntimeException("Expected one row to be affected but it was " + result);
    }
  }
  
  public static RowMapper<Integer> getSingleInt() {
    return ((rs, rowNum) -> rs.getInt(1));
  }
  
  public static RowMapper<Boolean> getSingleBoolean() {
    return ((rs, rowNum) -> rs.getBoolean(1));
  }
  
  public static RowMapper<Long> getSingleLong() {
    return ((rs, rowNum) -> rs.getLong(1));
  }
  
  public static RowMapper<String> getSingleString() {
    return ((rs, rowNum) -> rs.getString(1));
  }
  
  public static Integer getIntegerSqlVal(ResultSet rs, String field) throws SQLException {
    Object val = rs.getObject(field);
    return val == null ? null : (Integer) val;
  }
  
  public static Long getLongSqlVal(ResultSet rs, String field) throws SQLException {
    Object val = rs.getObject(field);
    return val == null ? null : (Long) val;
  }
  
  public static <T extends Enum<T>> T convertEnumFromSqlVal(
      ResultSet rs,
      String field,
      Class<T> enumType) throws SQLException {
    String val = rs.getString(field);
    return val == null ? null : Enum.valueOf(enumType, val);
  }
  
  public static Long getEpochSecsOrNullFromSqlTimestamp(ResultSet rs, String field)
      throws SQLException {
    LocalDateTime val = DateTimeUtil.sqlTimestampToLocal(rs.getTimestamp(field));
    return val == null ? null : DateTimeUtil.utcTimeToEpochSecs(val);
  }
  
  public static LocalDateTime getDateTimeOrNullFromSqlTimestamp(ResultSet rs, String field)
      throws SQLException {
    return DateTimeUtil.sqlTimestampToLocal(rs.getTimestamp(field));
  }
  
  public static long getEpochSecsFromSqlTimestamp(ResultSet rs, String field)
      throws SQLException {
    return DateTimeUtil.utcTimeToEpochSecs(
        DateTimeUtil.sqlTimestampToLocal(rs.getTimestamp(field)));
  }
  
  public static ZoneId getUTCZoneId() {
    return ZoneId.of("UTC");
  }
}
