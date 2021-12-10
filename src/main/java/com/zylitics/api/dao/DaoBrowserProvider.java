package com.zylitics.api.dao;

import com.zylitics.api.provider.BrowserProvider;
import com.zylitics.api.util.CommonUtil;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class DaoBrowserProvider extends AbstractDaoProvider implements BrowserProvider {
  
  DaoBrowserProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public boolean browserExists(String browserName, String browserVersion) {
    String sql = "SELECT count(*) FROM bt_browser_fe\n" +
        "WHERE name = :name AND display_version = :display_version";
    int result = jdbc.query(sql, new SqlParamsBuilder()
            .withOther("name", browserName)
            .withOther("display_version", browserVersion)
            .build(),
        CommonUtil.getSingleInt()).get(0);
    return result > 0;
  }
  
  @Override
  public String getLaterBrowsersVersion(String browserName) {
    String sql = "SELECT display_version FROM bt_browser_fe\n" +
        "WHERE name = :name ORDER by bt_browser_fe_id desc LIMIT 1";
    return jdbc.query(sql, new SqlParamsBuilder().withOther("name", browserName).build(),
        CommonUtil.getSingleString()).get(0);
  }
}
