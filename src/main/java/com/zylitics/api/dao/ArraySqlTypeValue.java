package com.zylitics.api.dao;

import org.springframework.jdbc.core.SqlTypeValue;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class ArraySqlTypeValue implements SqlTypeValue {
  
  private final Object[] value;
  
  ArraySqlTypeValue(Object[] value) {
    this.value = value;
  }
  
  @Override
  public void setTypeValue(PreparedStatement ps, int paramIndex, int sqlType, String typeName)
      throws SQLException {
    Array arr = ps.getConnection().createArrayOf(typeName, value);
    ps.setArray(paramIndex, arr);
  }
}
