package com.zylitics.api.dao;

import com.zylitics.api.provider.GlobalVarProvider;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class DaoGlobalVarProvider extends AbstractDaoProvider implements GlobalVarProvider {
  
  DaoGlobalVarProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public void captureGlobalVars(int projectId, int buildId) {
    String sql = "INSERT INTO bt_build_zwl_globals (bt_build_id, key, value)\n" +
        "SELECT :bt_build_id, key, value FROM zwl_globals\n" +
        "WHERE bt_project_id = :bt_project_id";
    SqlParameterSource namedParams = new SqlParamsBuilder()
        .withProject(projectId)
        .withInteger("bt_build_id", buildId).build();
    jdbc.update(sql, namedParams);
  }
}
