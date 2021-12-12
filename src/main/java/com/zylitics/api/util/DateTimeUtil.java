package com.zylitics.api.util;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.*;

public class DateTimeUtil {
  
  // OffsetDateTime is taken rather than ZonedDateTime because PGJDBC doesn't support it,
  // OffsetDateTime also works with ESDB.
  public static OffsetDateTime getCurrentUTC() {
    return OffsetDateTime.now(ZoneId.of("UTC"));
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
  
  public static long utcTimeToEpochSecs(LocalDateTime localDateTime) {
    return localDateTime.toInstant(ZoneOffset.UTC).getEpochSecond();
  }
  
  public static long sqlUTCTimestampToEpochSecs(Timestamp timestamp) {
    Preconditions.checkNotNull(timestamp, "Timestamp can't be null");
    return utcTimeToEpochSecs(sqlTimestampToLocal(timestamp));
  }
}
