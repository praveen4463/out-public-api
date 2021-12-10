package com.zylitics.api.dao;

import com.zylitics.api.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class Common extends AbstractDaoProvider {
  
  @Autowired
  Common(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  public Optional<Integer> verifyOrganizationProjectAndGetUserId(String apiKey, int projectId) {
    String sql = "SELECT zluser_id FROM bt_project\n" +
        "JOIN organization USING (organization_id)\n" +
        "WHERE bt_project_id = :bt_project_id AND api_key = :api_key";
    List<Integer> users = jdbc.query(sql,
        new SqlParamsBuilder()
            .withProject(projectId)
            .withOther("api_key", apiKey)
            .build(), CommonUtil.getSingleInt());
    
    if (users.size() == 0) {
      return Optional.empty();
    } else {
      return Optional.of(users.get(0));
    }
    
  }
}
