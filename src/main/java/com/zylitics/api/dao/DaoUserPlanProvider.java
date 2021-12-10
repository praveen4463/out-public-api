package com.zylitics.api.dao;

import com.zylitics.api.model.PlanType;
import com.zylitics.api.model.UsersPlan;
import com.zylitics.api.provider.UserPlanProvider;
import com.zylitics.api.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DaoUserPlanProvider extends AbstractDaoProvider implements UserPlanProvider {
  
  @Autowired
  DaoUserPlanProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public Optional<UsersPlan> getUserPlan(int userId) {
    String sql = "SELECT p.name AS plan_name, plan_type, p.display_name,\n" +
        "minutes, total_parallel, coalesce(minutes_consumed, 0) AS minutes_consumed,\n" +
        "billing_cycle_start AT TIME ZONE 'UTC' AS billing_cycle_start,\n" +
        "billing_cycle_planned_end AT TIME ZONE 'UTC' AS billing_cycle_planned_end\n" +
        "FROM zluser AS u\n" +
        "INNER JOIN organization AS o ON (u.organization_id = o.organization_id)\n" +
        "INNER JOIN quota AS q ON (o.organization_id = q.organization_id)\n" +
        "INNER JOIN plan AS p ON (q.plan_id = p.plan_id)\n" +
        "WHERE u.zluser_id = :zluser_id AND q.billing_cycle_actual_end IS NULL";
    List<UsersPlan> userPlan = jdbc.query(sql, new SqlParamsBuilder()
        .withInteger("zluser_id", userId)
        .build(),
        (rs, rowNum) -> new UsersPlan()
            .setPlanName(rs.getString("plan_name"))
            .setPlanType(PlanType.valueOf(rs.getString("plan_type")))
            .setDisplayName(rs.getString("display_name"))
            .setTotalMinutes(rs.getInt("minutes"))
            .setTotalParallel(rs.getInt("total_parallel"))
            .setConsumedMinutes(rs.getInt("minutes_consumed"))
            .setBillingCycleStart(
                DateTimeUtil.sqlUTCTimestampToEpochSecs(rs.getTimestamp("billing_cycle_start")))
            .setBillingCyclePlannedEnd(DateTimeUtil.sqlUTCTimestampToEpochSecs(
                rs.getTimestamp("billing_cycle_planned_end")))
    );
    
    if (userPlan.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(userPlan.get(0));
  }
}
