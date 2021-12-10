package com.zylitics.api.dao;

import com.zylitics.api.model.BuildVM;
import com.zylitics.api.provider.BuildVMProvider;
import com.zylitics.api.util.CommonUtil;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class DaoBuildVMProvider  extends AbstractDaoProvider implements BuildVMProvider {
  
  DaoBuildVMProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public int newBuildVM(BuildVM buildVM) {
    String sql = "INSERT INTO bt_build_vm (internal_ip, name, zone, delete_from_runner)\n" +
        "VALUES (:internal_ip, :name, :zone, :delete_from_runner) RETURNING bt_build_vm_id";
    return jdbc.query(sql, new SqlParamsBuilder()
        .withOther("internal_ip", buildVM.getInternalIp())
        .withOther("name", buildVM.getName())
        .withOther("zone", buildVM.getZone())
        .withBoolean("delete_from_runner", buildVM.isDeleteFromRunner())
        .build(), CommonUtil.getSingleInt()).get(0);
  }
}
