package com.zylitics.api.dao;

import com.zylitics.api.provider.BuildVarProvider;
import com.zylitics.api.util.CommonUtil;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.sql.JDBCType;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Repository
public class DaoBuildVarProvider extends AbstractDaoProvider implements BuildVarProvider {
  
  DaoBuildVarProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public void capturePrimaryBuildVarsOverridingGiven(int projectId,
                                                     @Nullable Map<String, String> override,
                                                     int buildId) {
    if (override == null) {
      override = new HashMap<>();
    }
    String insertSql = "INSERT INTO bt_build_zwl_build_variables (bt_build_id, key, value)\n";
    
    StringBuilder select = new StringBuilder();
    SqlParamsBuilder paramsBuilder = new SqlParamsBuilder();
    
    select.append("SELECT :bt_build_id, key, value FROM zwl_build_variables\n" +
        "WHERE bt_project_id = :bt_project_id\n" +
        "AND isPrimary = true AND key NOT IN (SELECT * FROM unnest(:overrideKeys))\n");
    
    paramsBuilder
        .withProject(projectId)
        .withInteger("bt_build_id", buildId)
        .withArray("overrideKeys", override.keySet().toArray(), JDBCType.VARCHAR);
    
    if (override.size() > 0) {
      select.append("UNION ALL\n");
    }
  
    int paramsUniqueCounter = 0;
    Iterator<String> keys = override.keySet().iterator();
    while (keys.hasNext()) {
      ++paramsUniqueCounter;
      String key = keys.next();
      select.append(String.format("SELECT :bt_build_id, :key%1$s, :value%1$s\n",
          paramsUniqueCounter));
      paramsBuilder.withVarchar("key" + paramsUniqueCounter, key);
      paramsBuilder.withOther("value" + paramsUniqueCounter, override.get(key));
      if (keys.hasNext()) {
        select.append("UNION ALL\n");
      }
    }
    
    int result = jdbc.update(insertSql + select, paramsBuilder.build());
    
    if (result < override.size()) {
      throw new RuntimeException("Couldn't insert build vars properly");
    }
  }
}
