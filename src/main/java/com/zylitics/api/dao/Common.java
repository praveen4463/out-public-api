package com.zylitics.api.dao;

import com.zylitics.api.model.UserDetail;
import com.zylitics.api.util.CommonUtil;
import org.apache.catalina.User;
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
  
  public Optional<UserDetail> verifyOrganizationProjectAndGetUserDetail(String apiKey,
                                                                        int projectId) {
    String sql = "SELECT zluser_id, o.organization_id FROM bt_project\n" +
        "JOIN organization o USING (organization_id)\n" +
        "WHERE bt_project_id = :bt_project_id AND api_key = :api_key";
    List<UserDetail> userDetails = jdbc.query(sql,
        new SqlParamsBuilder()
            .withProject(projectId)
            .withOther("api_key", apiKey)
            .build(), (rs, rowNum) ->
            new UserDetail()
                .setUserId(rs.getInt("zluser_id"))
                .setOrganizationId(rs.getInt("organization_id")));
    
    if (userDetails.size() == 0) {
      return Optional.empty();
    } else {
      return Optional.of(userDetails.get(0));
    }
  }
}
