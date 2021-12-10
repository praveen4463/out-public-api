package com.zylitics.api.dao;

import com.zylitics.api.model.BuildCapability;
import com.zylitics.api.provider.BuildCapabilityProvider;
import com.zylitics.api.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class DaoBuildCapabilityProvider extends AbstractDaoProvider
    implements BuildCapabilityProvider {
  
  @Autowired
  DaoBuildCapabilityProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public void captureCapability(BuildCapability buildCapability, int buildId) {
    String sql = "INSERT INTO bt_build_captured_capabilities (bt_build_id, name,\n" +
        "shot_take_test_shot, server_os, wd_browser_name,\n" +
        "wd_browser_version, wd_platform_name, wd_accept_insecure_certs,\n" +
        "wd_set_window_rect, wd_timeouts_script,\n" +
        "wd_timeouts_page_load, wd_timeouts_element_access, wd_strict_file_interactability,\n" +
        "wd_unhandled_prompt_behavior, wd_ie_element_scroll_behavior,\n" +
        "wd_ie_enable_persistent_hovering, wd_ie_require_window_focus,\n" +
        "wd_ie_disable_native_events, wd_ie_destructively_ensure_clean_session,\n" +
        "wd_ie_log_level, wd_chrome_verbose_logging, wd_chrome_silent_output,\n" +
        "wd_chrome_enable_network, wd_chrome_enable_page, wd_firefox_log_level,\n" +
        "wd_brw_start_maximize)\n" +
        "SELECT :bt_build_id, 'FROM_API',\n" +
        "true, :os, :browser,\n" +
        ":browser_version, :platform, false,\n" +
        "true, 10000,\n" +
        "30000, 10000, false,\n" +
        "'ignore', 'top',\n" +
        "false, false,\n" +
        "false, true,\n" +
        "'FATAL', false, false,\n" +
        "false, false, 'info',\n" +
        "true\n";
    int result = jdbc.update(sql, new SqlParamsBuilder()
        .withInteger("bt_build_id", buildId)
        .withVarchar("os", buildCapability.getOs())
        .withVarchar("browser", buildCapability.getBrowser())
        .withVarchar("browser_version", buildCapability.getBrowserVersion())
        .withVarchar("platform", buildCapability.getPlatform())
        .build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
}
