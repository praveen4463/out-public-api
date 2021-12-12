package com.zylitics.api.services;

import com.zylitics.api.controllers.RunnerService;
import com.zylitics.api.config.APICoreProperties;
import org.springframework.web.reactive.function.client.WebClient;

public class LocalRunnerService extends ProductionRunnerService implements RunnerService {
  
  public LocalRunnerService(APICoreProperties apiCoreProperties, WebClient webClient) {
    super(apiCoreProperties, webClient);
  }
  
  @Override
  int getVmAvailabilityTimeoutMin() {
    return 1;
  }
}
