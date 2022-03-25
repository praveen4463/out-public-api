package com.zylitics.api.services;

import com.zylitics.api.controllers.RunnerService;
import com.zylitics.api.config.APICoreProperties;
import com.zylitics.api.model.BuildVM;
import com.zylitics.api.util.UrlChecker;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ProductionRunnerService implements RunnerService {
  
  private static final int VM_AVAILABILITY_TIMEOUT_MIN = 2;
  
  private final WebClient webClient;
  
  private final APICoreProperties apiCoreProperties;
  
  public ProductionRunnerService(APICoreProperties apiCoreProperties, WebClient webClient) {
    this.apiCoreProperties = apiCoreProperties;
    this.webClient = webClient;
  }
  
  private Mono<NewSessionResponse> getNewSessionResponseAsync(BuildVM buildVM) {
    APICoreProperties.Services servicesProps = apiCoreProperties.getServices();
    String baseUrl = buildBaseUrl(buildVM.getInternalIp(), servicesProps);
    String statusEndpoint = "/status";
    // if any timeout occurs while polling for status, let exception throw
    new UrlChecker().waitUntilAvailable(getVmAvailabilityTimeoutMin(), TimeUnit.MINUTES,
        baseUrl + statusEndpoint);
    String buildsEndpoint = "/builds";
    // let exception throw when api returns error, we don't need to send that error to user.
    return webClient.post()
        .uri(baseUrl + buildsEndpoint)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new NewSessionRequest().setBuildId(buildVM.getBuildId()))
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(NewSessionResponse.class);
  }
  
  @Override
  public List<String> newSessions(List<BuildVM> buildVMs) {
    List<Mono<NewSessionResponse>> monos = new ArrayList<>();
    for (BuildVM buildVM : buildVMs) {
      monos.add(getNewSessionResponseAsync(buildVM));
    }
    Mono<List<NewSessionResponse>> responsesInProgress =
        Mono.zip(monos, objects -> Arrays.stream(objects).map(o ->
            (NewSessionResponse)o).collect(Collectors.toList()));
    List<NewSessionResponse> responses = responsesInProgress.block();
    if (responses == null) {
      throw new RuntimeException("Unexpectedly got empty response");
    }
    return responses.stream().map(NewSessionResponse::getSessionId).collect(Collectors.toList());
  }
  
  @Override
  public String newSession(BuildVM buildVM) {
    NewSessionResponse response = getNewSessionResponseAsync(buildVM).block();
    if (response == null) {
      throw new RuntimeException("Unexpectedly got empty response");
    }
    return response.getSessionId();
  }
  
  int getVmAvailabilityTimeoutMin() {
    return VM_AVAILABILITY_TIMEOUT_MIN;
  }
  
  protected String buildBaseUrl(String runnerIP, APICoreProperties.Services servicesProps) {
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
