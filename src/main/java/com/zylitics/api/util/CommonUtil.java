package com.zylitics.api.util;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.springframework.jdbc.core.RowMapper;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommonUtil {
  
  public static String constructStorageFilePath(@Nullable String prefix, String fileName) {
    if (Strings.isNullOrEmpty(prefix)) {
      return fileName;
    }
    return prefix + (prefix.endsWith("/") ? "" : "/") + fileName;
  }
  
  public static String getStorageFileNameWithoutPrefix(String fileName) {
    int index = fileName.lastIndexOf("/");
    if (index < 0) {
      return fileName;
    }
    return fileName.substring(index + 1);
  }
  
  public static String replaceUserId(String from, int userId) {
    return from.replace("USER_ID", String.valueOf(userId));
  }
  
  public static void validateSingleRowDbCommit(int result) {
    if (result != 1) {
      throw new RuntimeException("Expected one row to be affected but it was " + result);
    }
  }
  
  public static RowMapper<Integer> getSingleInt() {
    return ((rs, rowNum) -> rs.getInt(1));
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
  
  public static long getEpochSecsFromSqlTimestamp(ResultSet rs, String field)
      throws SQLException {
    return DateTimeUtil.utcTimeToEpochSecs(
        DateTimeUtil.sqlTimestampToLocal(rs.getTimestamp(field)));
  }
  
  public static List<Integer> commaDelToNumericList(@Nullable String commaDelimitedInt) {
    if (Strings.isNullOrEmpty(commaDelimitedInt)) {
      return new ArrayList<>(0);
    }
    return Splitter.on(",").omitEmptyStrings().trimResults()
        .splitToList(commaDelimitedInt).stream().map(Integer::parseInt).collect(Collectors.toList());
  }
}
