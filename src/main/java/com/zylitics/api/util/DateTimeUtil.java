package com.zylitics.api.util;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
  
  // OffsetDateTime is taken rather than ZonedDateTime because PGJDBC doesn't support it,
  // OffsetDateTime also works with ESDB.
  public static OffsetDateTime getCurrentUTC() {
    return OffsetDateTime.now(ZoneId.of("UTC"));
  }
  
  public static LocalDateTime getCurrentUTCAsLocal() {
    return LocalDateTime.now(ZoneId.of("UTC"));
  }
  
  // This method depends on the clock's timezone to get it's timezone, if clock's timezone is UTC,
  // returned timezone is UTC too.
  public static OffsetDateTime getCurrent(Clock clock) {
    return OffsetDateTime.now(clock);
  }
  
  public static LocalDateTime getCurrentLocal(Clock clock) {
    return LocalDateTime.now(clock);
  }
  
  /**
   * Does a safe conversion by checking null
   * @return null if given timestamp is null, else converted {@link LocalDateTime}
   */
  public static LocalDateTime sqlTimestampToLocal(@Nullable Timestamp timestamp) {
    if (timestamp == null) {
      return null;
    }
    return timestamp.toLocalDateTime();
  }
  
  public static LocalDateTime fromSqlTimestampIfNullGetGiven(LocalDateTime dateIfNull,
                                                             @Nullable Timestamp timestamp) {
    if (timestamp == null) {
      return dateIfNull;
    }
    return timestamp.toLocalDateTime();
  }
  
  public static long utcTimeToEpochSecs(LocalDateTime localDateTime) {
    return localDateTime.toInstant(ZoneOffset.UTC).getEpochSecond();
  }
  
  public static LocalDateTime epochSecsToUTCLocal(long epochSec) {
    return LocalDateTime.ofEpochSecond(epochSec, 0, ZoneOffset.UTC);
  }
  
  public static long sqlUTCTimestampToEpochSecs(Timestamp timestamp) {
    Preconditions.checkNotNull(timestamp, "Timestamp can't be null");
    return utcTimeToEpochSecs(sqlTimestampToLocal(timestamp));
  }
  
  public static String getFrondEndDisplayDate(LocalDateTime dateTime) {
    return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy, h:mm:ss a"));
  }
  
  public static OffsetDateTime isoLocalDateToUTCOffset(String isoLocalDate) {
    return OffsetDateTime.of(LocalDate.parse(isoLocalDate, DateTimeFormatter.ISO_LOCAL_DATE),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC);
  }
  
  public static OffsetDateTime fromUTCISODateTimeString(String utcISODateTime) {
    OffsetDateTime o = OffsetDateTime.parse(utcISODateTime, DateTimeFormatter.ISO_DATE_TIME);
    Preconditions.checkArgument(o.getOffset() == ZoneOffset.UTC, "Given date isn't in UTC");
    return o;
  }
}
