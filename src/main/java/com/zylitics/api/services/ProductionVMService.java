package com.zylitics.api.services;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.zylitics.api.SecretsManager;
import com.zylitics.api.controllers.VMService;
import com.zylitics.api.config.APICoreProperties;
import com.zylitics.api.model.BuildVM;
import com.zylitics.api.model.NewBuild;
import com.zylitics.api.model.NewBuildVM;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ProductionVMService implements VMService {
  
  private static final int RESPONSE_TIMEOUT_MIN = 10;
  
  private final Random random = new Random(); // keep it just one for entire application
  
  private final WebClient webClient;
  
  private final APICoreProperties apiCoreProperties;
  
  // TODO: !!currently the private endpoint of wzgp we're using is not authorizing requests and sending
  //  a auth header is useless. Do something in wzgp later so that all endpoints are secured even if
  //  private.
  public ProductionVMService(WebClient.Builder webClientBuilder,
                             APICoreProperties apiCoreProperties,
                             SecretsManager secretsManager) {
    this.apiCoreProperties = apiCoreProperties;
    APICoreProperties.Services services = apiCoreProperties.getServices();
    String secret = secretsManager.getSecretAsPlainText(services.getWzgpAuthSecretCloudFile());
    HttpClient httpClient = HttpClient.create()
        .responseTimeout(Duration.ofMinutes(RESPONSE_TIMEOUT_MIN));
    this.webClient = webClientBuilder
        .baseUrl(services.getWzgpEndpoint() + "/" + services.getWzgpVersion())
        .defaultHeaders(httpHeaders ->
            httpHeaders.setBasicAuth(HttpHeaders.encodeBasicAuth(services.getWzgpAuthUser(),
                secret, Charsets.UTF_8)))
        .clientConnector(new ReactorClientHttpConnector(httpClient)).build();
  }
  
  private Mono<GetVMResponse> getVMResponseAsync(NewBuildVM newBuildVM) {
    APICoreProperties.Services services = apiCoreProperties.getServices();
    GetVMRequest.BuildProperties buildProperties = new GetVMRequest.BuildProperties();
    buildProperties.setBuildId(newBuildVM.getBuildId());
  
    GetVMRequest.ResourceSearchParams resourceSearchParams =
        new GetVMRequest.ResourceSearchParams();
    resourceSearchParams
        .setOs(newBuildVM.getOs())
        .setBrowser(newBuildVM.getBrowserName())
        .setShots(true);
  
    GetVMRequest.GridProperties gridProperties = new GetVMRequest.GridProperties();
    gridProperties
        .setMachineType(services.getVmMachineType())
        .setCreateExternalIP(true)
        .setMetadata(ImmutableMap.of(
            "user-screen", newBuildVM.getDisplayResolution(),
            "user-desired-browser", newBuildVM.getBrowserName() + ";" +
                newBuildVM.getBrowserVersion(),
            "time-zone-with-dst", newBuildVM.getTimezone()
        ));
  
    GetVMRequest getVMRequest = new GetVMRequest();
    getVMRequest
        .setBuildProperties(buildProperties)
        .setResourceSearchParams(resourceSearchParams)
        .setGridProperties(gridProperties);
  
    List<String> availableZones = new ArrayList<>(services.getVmZones());
    int totalZones = availableZones.size();
    String randomZone = availableZones.get(random.nextInt(totalZones));
    String endpoint = String.format("/zones/%s/grids", randomZone);
    return webClient.post()
        .uri(uriBuilder -> uriBuilder
            .path(endpoint)
            .queryParam("requireRunningVM", newBuildVM.isRequireRunningVM())
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(getVMRequest)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(GetVMResponse.class);
  }
  
  private BuildVM getBuildVMFromResponse(GetVMResponse response, NewBuildVM newBuildVM) {
    return new BuildVM()
        .setInternalIp(response.getGridInternalIP())
        .setName(response.getGridName())
        .setZone(response.getZone())
        .setDeleteFromRunner(true)
        .setBuildId(newBuildVM.getBuildId());
  }
  
  @Override
  public List<BuildVM> newBuildVMs(List<NewBuildVM> newBuildVMs) {
    List<Mono<GetVMResponse>> monos = new ArrayList<>();
    for (NewBuildVM newBuildVM : newBuildVMs) {
      monos.add(getVMResponseAsync(newBuildVM));
    }
    Mono<List<GetVMResponse>> responsesInProgress =
        Mono.zip(monos, objects -> Arrays.stream(objects).map(o ->
            (GetVMResponse)o).collect(Collectors.toList()));
    List<GetVMResponse> responses = responsesInProgress.block();
    if (responses == null) {
      throw new RuntimeException("Unexpectedly got empty response");
    }
    return IntStream.range(0, responses.size())
        .mapToObj(i -> getBuildVMFromResponse(responses.get(i), newBuildVMs.get(i)))
        .collect(Collectors.toList());
  }
  
  @Override
  public BuildVM newBuildVM(NewBuildVM newBuildVM) {
    GetVMResponse response = getVMResponseAsync(newBuildVM).block();
    if (response == null) {
      throw new RuntimeException("Unexpectedly got empty response");
    }
    return getBuildVMFromResponse(response, newBuildVM);
  }
  
  private static class GetVMRequest {
  
    private BuildProperties buildProperties;
  
    private ResourceSearchParams resourceSearchParams;
  
    private GridProperties gridProperties;
  
    public BuildProperties getBuildProperties() {
      return buildProperties;
    }
  
    public GetVMRequest setBuildProperties(BuildProperties buildProperties) {
      this.buildProperties = buildProperties;
      return this;
    }
  
    public ResourceSearchParams getResourceSearchParams() {
      return resourceSearchParams;
    }
  
    public GetVMRequest setResourceSearchParams(ResourceSearchParams resourceSearchParams) {
      this.resourceSearchParams = resourceSearchParams;
      return this;
    }
  
    public GridProperties getGridProperties() {
      return gridProperties;
    }
  
    public GetVMRequest setGridProperties(GridProperties gridProperties) {
      this.gridProperties = gridProperties;
      return this;
    }
  
    private static class BuildProperties {
      
      private int buildId;
  
      public int getBuildId() {
        return buildId;
      }
  
      public BuildProperties setBuildId(int buildId) {
        this.buildId = buildId;
        return this;
      }
    }
  
    private static class ResourceSearchParams {
  
      private String os;
      private String browser;
      private boolean shots;
  
      public String getOs() {
        return os;
      }
  
      public ResourceSearchParams setOs(String os) {
        this.os = os;
        return this;
      }
  
      public String getBrowser() {
        return browser;
      }
  
      public ResourceSearchParams setBrowser(String browser) {
        this.browser = browser;
        return this;
      }
  
      public boolean isShots() {
        return shots;
      }
  
      public ResourceSearchParams setShots(boolean shots) {
        this.shots = shots;
        return this;
      }
    }
  
    private static class GridProperties {
  
      private String machineType;
  
      private boolean createExternalIP;
  
      private Map<String, String> metadata;
  
      public String getMachineType() {
        return machineType;
      }
  
      public GridProperties setMachineType(String machineType) {
        this.machineType = machineType;
        return this;
      }
  
      public boolean isCreateExternalIP() {
        return createExternalIP;
      }
  
      public GridProperties setCreateExternalIP(boolean createExternalIP) {
        this.createExternalIP = createExternalIP;
        return this;
      }
  
      public Map<String, String> getMetadata() {
        return metadata;
      }
  
      public GridProperties setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
      }
    }
  }
  
  private static class GetVMResponse {
    
    private String gridInternalIP;
    
    private String gridName;
    
    private String zone;
  
    public String getGridInternalIP() {
      return gridInternalIP;
    }
  
    public GetVMResponse setGridInternalIP(String gridInternalIP) {
      this.gridInternalIP = gridInternalIP;
      return this;
    }
  
    public String getGridName() {
      return gridName;
    }
  
    public GetVMResponse setGridName(String gridName) {
      this.gridName = gridName;
      return this;
    }
  
    public String getZone() {
      return zone;
    }
  
    public GetVMResponse setZone(String zone) {
      this.zone = zone;
      return this;
    }
  }
}
