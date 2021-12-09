package com.zylitics.api.dao;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public abstract class AbstractDaoProvider {
  
  // https://docs.spring.io/spring/docs/current/spring-framework-reference/data-access.html#jdbc-NamedParameterJdbcTemplate
  // when instantiated, it created a JDBCTemplate and wraps it to use it for processing queries, we
  // can get the wrapped JDBCTemplate using getJdbcOperations()
  final NamedParameterJdbcTemplate jdbc;
  
  AbstractDaoProvider(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }
}
