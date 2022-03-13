package com.zylitics.api.services;

import com.google.common.base.Preconditions;
import com.zylitics.api.controllers.VMService;
import com.zylitics.api.config.APICoreProperties;
import com.zylitics.api.model.BuildVM;
import com.zylitics.api.model.NewBuildVM;

import java.util.List;

public class LocalVMService implements VMService {
  
  private final APICoreProperties apiCoreProperties;
  
  public LocalVMService(APICoreProperties apiCoreProperties) {
    this.apiCoreProperties = apiCoreProperties;
  }
  
  @Override
  public BuildVM newBuildVM(NewBuildVM newBuildVM) {
    String localVm = System.getenv(apiCoreProperties.getServices().getLocalVmEnvVar());
    Preconditions.checkNotNull(localVm, "LocalVm env var is not set");
    return new BuildVM().setName("local").setZone("local").setInternalIp(localVm);
  }
  
  @Override
  public List<BuildVM> newBuildVMs(List<NewBuildVM> newBuildVMs) {
    throw new RuntimeException("Not implemented");
  }
}
