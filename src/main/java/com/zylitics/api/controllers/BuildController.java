package com.zylitics.api.controllers;

import com.zylitics.api.config.APICoreProperties;
import com.zylitics.api.config.BrowserConfig;
import com.zylitics.api.config.OSConfig;
import com.zylitics.api.dao.Common;
import com.zylitics.api.exception.HttpRequestException;
import com.zylitics.api.exception.UnauthorizedException;
import com.zylitics.api.internal.FilesParser;
import com.zylitics.api.model.*;
import com.zylitics.api.provider.*;
import com.zylitics.api.util.OSDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@RestController
@RequestMapping("${app-short-version}")
public class BuildController extends AbstractController {
  
  private static final String NEW_SESSION_ERROR = "An internal server error occurred" +
      " while creating new build.";
  
  private static final String VM_NOT_CREATED_ERROR = "Couldn't create a VM for this build." +
      " Please try in a few minutes or contact us if problem persists";
  
  private static final Logger LOG = LoggerFactory.getLogger(BuildController.class);
  
  private static final Pattern DIS_RES_PATTERN = Pattern.compile("\\d{3,4}x\\d{3,4}");
  
  private final APICoreProperties apiCoreProperties;
  
  private final Common common;
  
  private final BuildProvider buildProvider;
  
  private final BuildRequestProvider buildRequestProvider;
  
  private final UserPlanProvider userPlanProvider;
  
  private final BrowserProvider browserProvider;
  
  private final VMService vmService;
  
  private final RunnerService runnerService;
  
  public BuildController(BuildProvider buildProvider,
                         BuildRequestProvider buildRequestProvider,
                         VMService vmService,
                         RunnerService runnerService,
                         UserPlanProvider userPlanProvider,
                         BrowserProvider browserProvider,
                         APICoreProperties apiCoreProperties,
                         Common common) {
    this.buildProvider = buildProvider;
    this.buildRequestProvider = buildRequestProvider;
    this.vmService = vmService;
    this.runnerService = runnerService;
    this.userPlanProvider = userPlanProvider;
    this.browserProvider = browserProvider;
    this.apiCoreProperties = apiCoreProperties;
    this.common = common;
  }
  
  @PostMapping("/projects/{projectId}/builds")
  @SuppressWarnings("unused")
  public ResponseEntity<?> newBuild(
      @Validated @RequestBody(required = false) BuildRunConfig config,
      @PathVariable @Min(1) int projectId,
      @RequestHeader(API_KEY_REQ_HEADER) String apiKey
  ) {
    if (config == null) {
      config = new BuildRunConfig();
    }
    int userId = common.verifyOrganizationProjectAndGetUserId(apiKey, projectId)
        .orElseThrow(() -> new UnauthorizedException("Either the API key is invalid or given" +
            " project wasn't found."));
    
    NewBuild newBuild = new NewBuild();
    newBuild.setBuildName(config.getBuildName());
    if (config.getWaitForCompletion() == null || config.getWaitForCompletion()) {
      newBuild.setSourceType(BuildSourceType.CI);
    } else {
      newBuild.setSourceType(BuildSourceType.NOT_IDE);
    }
    // Same bucket for all as this api is accessed org wide, and we don't want to set it to a
    // particular user.
    newBuild.setShotBucket(apiCoreProperties.getStorage().getShotBucketUsc());
    newBuild.setFiles(config.getFiles());
    newBuild.setBuildCapability(deduceBuildCaps(config));
    newBuild.setBuildConfig(deduceBuildConfig(config));
    
    long buildRequestId = getNewBuildRequest(newBuild.getSourceType(), userId);
    try {
      int buildId = createNewBuild(buildRequestId, newBuild, projectId, userId);
      SessionFailureReason sessionFailureReason = null;
      try {
        buildProvider.updateSessionRequestStart(buildId);
        // send a request to create/find a VM
        BuildVM buildVM;
        try {
          buildVM = vmService.newBuildVM(new NewBuildVM()
              .setBuildId(buildId)
              .setRequireRunningVM(false)
              .setDisplayResolution(newBuild.getBuildConfig().getDisplayResolution())
              .setTimezone(newBuild.getBuildConfig().getTimezone())
              .setBrowserName(newBuild.getBuildCapability().getBrowser())
              .setBrowserVersion(newBuild.getBuildCapability().getBrowserVersion())
              .setOs(newBuild.getBuildCapability().getOs()));
          // set deleteFromRunner
          buildVM.setDeleteFromRunner(true);
        } catch (Throwable t) {
          LOG.error("Couldn't create buildVM", t);
          sessionFailureReason = SessionFailureReason.VM_NOT_CREATED;
          throw new HttpRequestException(HttpStatus.INTERNAL_SERVER_ERROR, VM_NOT_CREATED_ERROR);
        }
        // save buildVM details to vm table
        buildProvider.createAndUpdateVM(buildVM, buildId);
        // start new session as we've a VM
        String sessionId;
        try {
          sessionId =
              runnerService.newSession(buildVM.getInternalIp(), buildId);
        } catch (Throwable t) {
          LOG.error("Couldn't start a new session", t);
          sessionFailureReason = SessionFailureReason.NEW_SESSION_ERROR;
          throw new HttpRequestException(HttpStatus.INTERNAL_SERVER_ERROR, NEW_SESSION_ERROR);
        }
        // updates session_request_end_date
        buildProvider.updateSession(sessionId, buildId);
        
        NewBuildResponse res = new NewBuildResponse().setBuildId(buildId);
        if (newBuild.getSourceType() == BuildSourceType.CI) {
          Build build = buildProvider.getBuild(buildId)
              .orElseThrow(() -> new RuntimeException("Couldn't fetch build " + buildId));
          res.setStatus(build.getFinalStatus().toString().toLowerCase(Locale.US));
          res.setError(build.getError());
        } else {
          res.setStatus(TestStatus.RUNNING.toString().toLowerCase(Locale.US));
        }
        return ResponseEntity.ok(res);
      } catch (Throwable t) {
        markBuildRequestCompleted(buildRequestId);
        // updates session_request_end_date
        buildProvider.updateOnSessionFailure(
            sessionFailureReason != null ? sessionFailureReason : SessionFailureReason.EXCEPTION,
            sessionFailureReason == SessionFailureReason.VM_NOT_CREATED
                ? VM_NOT_CREATED_ERROR
                : NEW_SESSION_ERROR,
            buildId);
        throw t;
      }
    } catch (HttpRequestException httpRequestException) {
      return sendError(httpRequestException.getStatus(), httpRequestException.getMessage());
    }
  }
  
  // !! make sure we mark build request completed whenever there is an error
  private int createNewBuild(long buildRequestId,
                             NewBuild newBuild,
                             int projectId,
                             int userId) {
    try {
      validateNewBuildRequestQuota(userId);
      // parallel, minutes quota verified, let's jump on create build.
      return buildProvider.newBuild(newBuild, buildRequestId, projectId);
    } catch (Throwable t) {
      markBuildRequestCompleted(buildRequestId);
      throw t;
    }
  }
  
  BuildCapability deduceBuildCaps(BuildRunConfig config) {
    String os;
    String browser;
    String browserVersion;
    String platform;
  
    BuildRunConfig.BuildCapability configBCaps = config.getBuildCapability();
    if (configBCaps == null) {
      configBCaps = new BuildRunConfig.BuildCapability();
    }
    OSDescriptor osDescriptor = OSConfig.understandOs(configBCaps.getOs() != null
        ? configBCaps.getOs() : "win10");
    os = osDescriptor.getName();
    platform = osDescriptor.getPlatform();
  
    browser = BrowserConfig.understandBrowser(configBCaps.getBrowser() != null
        ? configBCaps.getBrowser() : "chrome");
    browserVersion = configBCaps.getBrowserVersion() != null
        ? configBCaps.getBrowserVersion() : browserProvider.getLaterBrowsersVersion(browser);
    
    if (configBCaps.getBrowser() != null && configBCaps.getBrowserVersion() != null) {
      if (!browserProvider.browserExists(browser, browserVersion)) {
        throw new IllegalArgumentException("Given browser version doesn't exist");
      }
    }
    return new BuildCapability()
        .setOs(os)
        .setPlatform(platform)
        .setBrowser(browser)
        .setBrowserVersion(browserVersion);
  }
  
  BuildConfig deduceBuildConfig(BuildRunConfig config) {
    String disRes;
    BuildRunConfig.BuildConfig configBConf = config.getBuildConfig();
    if (configBConf == null) {
      configBConf = new BuildRunConfig.BuildConfig();
    }
    
    if (configBConf.getDisplayResolution() != null) {
      disRes = configBConf.getDisplayResolution();
      if (!DIS_RES_PATTERN.matcher(disRes).matches()) {
        throw new IllegalArgumentException("Given display resolution is invalid");
      }
    } else {
      disRes = "1366x768";
    }
    return new BuildConfig()
        .setDisplayResolution(disRes)
        .setTimezone(configBConf.getTimezone())
        .setRetryFailedTestsUpto(configBConf.getRetryFailedTestsUpto())
        .setBuildVars(configBConf.getBuildVars());
  }
  
  private long getNewBuildRequest(BuildSourceType sourceType, int userId) {
    return buildRequestProvider.newBuildRequest(new BuildRequest()
        .setBuildSourceType(sourceType).setUserId(userId));
  }
  
  private void validateNewBuildRequestQuota(int userId) {
    UsersPlan usersPlan = userPlanProvider.getUserPlan(userId)
        .orElseThrow(() -> new RuntimeException(userId + " User plan not found"));
    List<BuildRequest> buildRequests = buildRequestProvider.getCurrentBuildRequests(userId);
    int totalBuildRequests = buildRequests.size();
    
    if (totalBuildRequests > usersPlan.getTotalParallel()) {
      throw new HttpRequestException(HttpStatus.TOO_MANY_REQUESTS,
          "Total parallel builds limit reached." +
              " If you're starting several builds together, please slow down and wait for few" +
              " moments in between build runs");
    }
    if (usersPlan.getPlanType() == PlanType.FREE &&
        usersPlan.getTotalMinutes() - usersPlan.getConsumedMinutes() < 1) {
      throw new HttpRequestException(HttpStatus.FORBIDDEN,
          "You've exhausted plan's minutes quota" +
              ", please upgrade or contact us for additional testing minutes");
    }
  }
  
  private void markBuildRequestCompleted(long buildRequestId) {
    buildRequestProvider.markBuildRequestCompleted(buildRequestId);
  }
}
