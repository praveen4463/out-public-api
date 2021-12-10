package com.zylitics.api.services;

import com.zylitics.api.controllers.RunnerService;
import com.zylitics.api.config.APICoreProperties;
import com.zylitics.api.util.UrlChecker;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.TimeUnit;

public class ProductionRunnerService implements RunnerService {
  
  private static final int VM_AVAILABILITY_TIMEOUT_MIN = 2;
  
  private final WebClient webClient;
  
  private final APICoreProperties apiCoreProperties;
  
  public ProductionRunnerService(APICoreProperties apiCoreProperties, WebClient webClient) {
    this.apiCoreProperties = apiCoreProperties;
    this.webClient = webClient;
  }
  
  @Override
  public String newSession(String runnerIP, int buildId) {
    APICoreProperties.Services servicesProps = apiCoreProperties.getServices();
    String baseUrl = buildBaseUrl(runnerIP, servicesProps);
    String statusEndpoint = "/status";
    // if any timeout occurs while polling for status, let exception throw
    new UrlChecker().waitUntilAvailable(VM_AVAILABILITY_TIMEOUT_MIN, TimeUnit.MINUTES,
        baseUrl + statusEndpoint);
    String buildsEndpoint = "/builds";
    // let exception throw when api returns error, we don't need to send that error to user.
    NewSessionResponse response = webClient.post()
        .uri(baseUrl + buildsEndpoint)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new NewSessionRequest().setBuildId(buildId))
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(NewSessionResponse.class).block();
    if (response == null) {
      throw new RuntimeException("Unexpectedly got empty response");
    }
    return response.getSessionId();
  }
  
  private String buildBaseUrl(String runnerIP, APICoreProperties.Services servicesProps) {
    return String.format("http://%s:%s/%s", runnerIP, servicesProps.getBtbrPort(),
        servicesProps.getBtbrVersion());
  }
  
  private static class NewSessionRequest {
    
    private int buildId;
  
    public int getBuildId() {
      return buildId;
    }
  
    public NewSessionRequest setBuildId(int buildId) {
      this.buildId = buildId;
      return this;
    }
  }
  
  private static class NewSessionResponse {
    
    private String sessionId;
  
    public String getSessionId() {
      return sessionId;
    }
  
    @SuppressWarnings("unused")
    public NewSessionResponse setSessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
    }
  }
}
