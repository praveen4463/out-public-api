package com.zylitics.api.services;

import com.zylitics.api.controllers.RunnerService;
import com.zylitics.api.config.APICoreProperties;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Random;

public class LocalRunnerService extends ProductionRunnerService implements RunnerService {
  
  private static final int[] PORTS = {8080, 8081, 8082, 8083};
  
  private final Random random = new Random();
  
  public LocalRunnerService(APICoreProperties apiCoreProperties, WebClient webClient) {
    super(apiCoreProperties, webClient);
  }
  
  @Override
  int getVmAvailabilityTimeoutMin() {
    return 1;
  }
  
  @Override
  protected String buildBaseUrl(String runnerIP, APICoreProperties.Services servicesProps) {
    int port = PORTS[random.nextInt(PORTS.length)];
    return String.format("http://%s:%s/%s", runnerIP, port, servicesProps.getBtbrVersion());
  }
  
  /* This is disabled until we've made fix in btbr for detecting and stopping multiple concurrent requests
  // Works for parallel builds too. Checks repeatedly the same machine on different ports (so that
  // we can run more than 1 runner on same machine). If one is found, checks to see whether a build
  // is already running on that by sending a wrong billingId and checking on the response.
  @Override
  protected String buildBaseUrl(String runnerIP, APICoreProperties.Services servicesProps) {
    String baseUrl;
    String buildsEndpoint = "/builds";
    for (int port : PORTS) {
      baseUrl = String.format("http://%s:%s/%s", runnerIP, port,
          servicesProps.getBtbrVersion());
      try {
        new UrlChecker().waitUntilAvailable(10, TimeUnit.SECONDS, baseUrl + "/status");
        try {
          webClient.post()
              .uri(baseUrl + buildsEndpoint)
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(new NewSessionRequest().setBuildId(0))
              .accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .toBodilessEntity().block();
        } catch (WebClientResponseException webClientResponseException) {
          if (webClientResponseException.getStatusCode() != HttpStatus.TOO_MANY_REQUESTS) {
            return baseUrl;
          }
        }
      } catch (RuntimeException ex) {
        // do nothing
      }
    }
    throw new RuntimeException("No local runner found running");
  } */
}
