package com.zylitics.api.dao;

import com.zylitics.api.util.DateTimeUtil;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.annotation.Nullable;
import java.sql.JDBCType;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

class SqlParamsBuilder {
  
  private final Map<String, SqlParameterValue> params = new HashMap<>();
  
  SqlParamsBuilder() {}
  
  SqlParamsBuilder withOrganization(int organizationId) {
    params.put("organization_id", new SqlParameterValue(Types.INTEGER, organizationId));
    return this;
  }
  
  SqlParamsBuilder withProject(int projectId) {
    params.put("bt_project_id", new SqlParameterValue(Types.INTEGER, projectId));
    return this;
  }
  
  SqlParamsBuilder withCreateDate() {
    params.put("create_date", new SqlParameterValue(Types.TIMESTAMP_WITH_TIMEZONE,
        DateTimeUtil.getCurrentUTC()));
    return this;
  }
  
  SqlParamsBuilder withInteger(String name, int value) {
    params.put(name, new SqlParameterValue(Types.INTEGER, value));
    return this;
  }
  
  SqlParamsBuilder withInteger(String name, Integer value) {
    params.put(name, new SqlParameterValue(getIntendedOrNullType(Types.INTEGER, value), value));
    return this;
  }
  
  SqlParamsBuilder withOther(String name, Object value) {
    params.put(name, new SqlParameterValue(getIntendedOrNullType(Types.OTHER, value), value));
    return this;
  }
  
  SqlParamsBuilder withVarchar(String name, String value) {
    params.put(name, new SqlParameterValue(getIntendedOrNullType(Types.VARCHAR, value), value));
    return this;
  }
  
  SqlParamsBuilder withBoolean(String name, boolean value) {
    params.put(name, new SqlParameterValue(Types.BOOLEAN, value));
    return this;
  }
  
  SqlParamsBuilder withTimestampTimezone(String name, OffsetDateTime value) {
    params.put(name,
        new SqlParameterValue(getIntendedOrNullType(Types.TIMESTAMP_WITH_TIMEZONE, value), value));
    return this;
  }
  
  SqlParamsBuilder withBigint(String name, long value) {
    params.put(name, new SqlParameterValue(Types.BIGINT, value));
    return this;
  }
  
  SqlParamsBuilder withArray(String name, Object[] value,
                             @SuppressWarnings("SameParameterValue") JDBCType elementType) {
    if (value == null) {
      params.put(name, new SqlParameterValue(Types.NULL, null));
    } else {
      params.put(name, new SqlParameterValue(Types.ARRAY, elementType.getName()
          , new ArraySqlTypeValue(value)));
    }
    return this;
  }
  
  SqlParameterSource build() {
    return new MapSqlParameterSource(params);
  }
  
  private <T> int getIntendedOrNullType(int intended, @Nullable T value) {
    if (value == null) {
      return Types.NULL;
    }
    return intended;
  }
}
