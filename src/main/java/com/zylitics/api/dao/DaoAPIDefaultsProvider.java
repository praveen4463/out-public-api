package com.zylitics.api.dao;

import com.zylitics.api.model.APIDefaults;
import com.zylitics.api.provider.APIDefaultsProvider;
import com.zylitics.api.util.CommonUtil;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DaoAPIDefaultsProvider extends AbstractDaoProvider
    implements APIDefaultsProvider {
  
  DaoAPIDefaultsProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public Optional<APIDefaults> getApiDefaults(int organizationId) {
    String sql = "SELECT retry_failed_test_upto\n" +
        "FROM api_defaults\n" +
        "WHERE organization_id = :organization_id";
    List<APIDefaults> apiDefaultsList = jdbc.query(
        sql,
        new SqlParamsBuilder().withOrganization(organizationId).build(),
        (rs, rowNum ) -> new APIDefaults()
            .setRetryFailedTestsUpto(CommonUtil.getIntegerSqlVal(rs, "retry_failed_test_upto"))
    );
    if (apiDefaultsList.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(apiDefaultsList.get(0));
  }
}
