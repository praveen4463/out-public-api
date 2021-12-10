package com.zylitics.api.dao;

import com.zylitics.api.model.BuildRequest;
import com.zylitics.api.model.BuildSourceType;
import com.zylitics.api.provider.BuildRequestProvider;
import com.zylitics.api.util.CommonUtil;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DaoBuildRequestProvider extends AbstractDaoProvider implements BuildRequestProvider {
  
  DaoBuildRequestProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public long newBuildRequest(BuildRequest buildRequest) {
    String sql = "INSERT INTO bt_build_request (source_type, zluser_id)\n" +
        "VALUES (:source_type, :zluser_id) RETURNING bt_build_request_id";
    return jdbc.query(sql, new SqlParamsBuilder()
        .withInteger("zluser_id", buildRequest.getUserId())
        .withOther("source_type", buildRequest.getBuildSourceType()).build(),
        CommonUtil.getSingleLong()).get(0);
  }
  
  @Override
  public List<BuildRequest> getCurrentBuildRequests(int userId) {
    String sql = "SELECT source_type FROM bt_build_request WHERE zluser_id = :zluser_id\n" +
        "AND completed = false";
    return jdbc.query(sql, new SqlParamsBuilder()
        .withInteger("zluser_id", userId).build(), (rs, rowNum) ->
        new BuildRequest()
            .setBuildSourceType(BuildSourceType.valueOf(rs.getString("source_type")))
            .setUserId(userId));
  }
  
  @Override
  public void markBuildRequestCompleted(long buildRequestId) {
    String sql = "UPDATE bt_build_request SET completed = true\n" +
        " WHERE bt_build_request_id = :bt_build_request_id";
    int result = jdbc.update(sql, new SqlParamsBuilder()
        .withBigint("bt_build_request_id", buildRequestId).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
}
