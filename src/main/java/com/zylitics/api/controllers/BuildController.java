package com.zylitics.api.controllers;

import com.zylitics.api.config.APICoreProperties;
import com.zylitics.api.config.BrowserConfig;
import com.zylitics.api.config.OSConfig;
import com.zylitics.api.dao.Common;
import com.zylitics.api.exception.HttpRequestException;
import com.zylitics.api.exception.UnauthorizedException;
import com.zylitics.api.handlers.ParallelBuildCompletionEmailHandler;
import com.zylitics.api.model.*;
import com.zylitics.api.provider.*;
import com.zylitics.api.model.OSDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

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
  
  private final APIDefaultsProvider apiDefaultsProvider;
  
  private final TestProvider testProvider;
  
  private final VMService vmService;
  
  private final RunnerService runnerService;
  
  private final ParallelBuildCompletionEmailHandler parallelBuildCompletionEmailHandler;
  
  public BuildController(BuildProvider buildProvider,
                         BuildRequestProvider buildRequestProvider,
                         VMService vmService,
                         RunnerService runnerService,
                         UserPlanProvider userPlanProvider,
                         BrowserProvider browserProvider,
                         APIDefaultsProvider apiDefaultsProvider,
                         TestProvider testProvider,
                         APICoreProperties apiCoreProperties,
                         ParallelBuildCompletionEmailHandler parallelBuildCompletionEmailHandler,
                         Common common) {
    this.buildProvider = buildProvider;
    this.buildRequestProvider = buildRequestProvider;
    this.vmService = vmService;
    this.runnerService = runnerService;
    this.userPlanProvider = userPlanProvider;
    this.browserProvider = browserProvider;
    this.apiDefaultsProvider = apiDefaultsProvider;
    this.testProvider = testProvider;
    this.apiCoreProperties = apiCoreProperties;
    this.parallelBuildCompletionEmailHandler = parallelBuildCompletionEmailHandler;
    this.common = common;
  }
  
  // !!This is a temporary way to enable parallel builds. A few things may not look right but that's
  // fine for now until we do it properly, later.
  private ResponseEntity<?> newParallelBuild(UserDetail userDetail,
                                             BuildRunConfig buildRunConfig,
                                             BuildConfig normalizedBuildConfig,
                                             int projectId) {
    boolean notifyOnComplete = normalizedBuildConfig.getNotifyOnCompletion();
    int totalParallel = normalizedBuildConfig.getTotalParallel();
    BuildSourceType presetBuildSourceType = BuildSourceType.CI;
  
    BuildCapability normalizedCap = deduceBuildCaps(buildRunConfig);
    // In all cases, we want the runner to not notify on completion because it will be handled
    // from this method, when enabled.
    BuildConfig normalizedParallelBuildConfig = normalizedBuildConfig.setNotifyOnCompletion(false);
    
    List<NewBuild> newBuilds = new ArrayList<>();
    List<Long> buildRequestIds = new ArrayList<>();
    List<Integer> buildIds;
    List<String> sessionIds;
    
    try {
      // Check for quota in the beginning and bail out if problems found.
      validateNewBuildRequestQuota(userDetail.getUserId(), totalParallel);
      
      for (int i = 0; i < totalParallel; i++) {
        NewBuild newBuild = new NewBuild();
        //  Add a PartN suffix to build name to easily identify build parts
        newBuild.setBuildName(buildRunConfig.getBuildName() + " - Part" + (i + 1));
        // Always set the source to something that makes runner sending a response only after build is
        // completed. This is done so that this api method can trigger post build completion steps
        // such as sending a composite email.
        // !! This will ignore 'waitForCompletion' settings for now. If we respect this setting and
        // still want to perform completion steps, we will have to poll the db/runner until the completion,
        // but I'm avoiding that complication for now.
        newBuild.setSourceType(presetBuildSourceType);
        // Same bucket for all as this api is accessed org wide, and we don't want to set it to a
        // particular user.
        newBuild.setShotBucket(apiCoreProperties.getStorage().getShotBucketUsc());
        newBuild.setBuildCapability(normalizedCap);
        newBuild.setBuildConfig(normalizedParallelBuildConfig);
    
        // create 1 build request for every build and don't use the totalParallel field because
        // build.buildRequestId is unique.
        long buildRequestId = getNewBuildRequest(newBuild.getSourceType(), userDetail.getUserId());
        newBuild.setBuildRequestId(buildRequestId);
    
        buildRequestIds.add(buildRequestId);
        newBuilds.add(newBuild);
      }
      
      buildIds = buildProvider.createNewBuilds(newBuilds,
          buildRunConfig.getFiles(),
          projectId,
          "There are lesser tests in given project than the specified number of parallels." +
              "Number of tests must be equal to or greater than the given parallel number.");
      
      List<NewBuildVM> newBuildVMS = new ArrayList<>();
      buildIds.forEach(buildId -> {
        buildProvider.updateSessionRequestStart(buildId);
        
        NewBuildVM newBuildVM = new NewBuildVM()
            .setBuildId(buildId)
            .setRequireRunningVM(false)
            .setDisplayResolution(normalizedParallelBuildConfig.getDisplayResolution())
            .setTimezone(normalizedParallelBuildConfig.getTimezone())
            .setBrowserName(normalizedCap.getBrowser())
            .setBrowserVersion(normalizedCap.getBrowserVersion())
            .setOs(normalizedCap.getOs());
        newBuildVMS.add(newBuildVM);
      });
  
      SessionFailureReason sessionFailureReason = null;
      try {
        List<BuildVM> buildVMS;
        try {
          buildVMS = vmService.newBuildVMs(newBuildVMS);
        } catch (Throwable t) {
          // TODO: In case of partial failure, some VMs will remain dangling and will need to be
          //  deleted either from here or manually. Keep an eye on logs and see what can be done.
          LOG.error("Couldn't create parallel buildVMs", t);
          sessionFailureReason = SessionFailureReason.VM_NOT_CREATED;
          throw new HttpRequestException(HttpStatus.INTERNAL_SERVER_ERROR, VM_NOT_CREATED_ERROR);
        }
        buildVMS.forEach(buildProvider::createAndUpdateVM);
        
        try {
          sessionIds = runnerService.newSessions(buildVMS);
        } catch (Throwable t) {
          // TODO: In case of partial failure, some builds will be running while some other won't.
          //  We need to detect a failure and send a STOP to running builds.
          //  Keep an eye on logs and see when will we have to do this part.
          LOG.error("Couldn't start new parallel sessions", t);
          sessionFailureReason = SessionFailureReason.NEW_SESSION_ERROR;
          throw new HttpRequestException(HttpStatus.INTERNAL_SERVER_ERROR, NEW_SESSION_ERROR);
        }
      } catch (Throwable t) {
        // updates session_request_end_date
        for (int buildId : buildIds) {
          buildProvider.updateOnSessionFailure(
              sessionFailureReason != null ? sessionFailureReason : SessionFailureReason.EXCEPTION,
              sessionFailureReason == SessionFailureReason.VM_NOT_CREATED
                  ? VM_NOT_CREATED_ERROR
                  : NEW_SESSION_ERROR,
              buildId);
        }
        throw t;
      }
    } catch (Throwable t) {
      buildRequestIds.forEach(this::markBuildRequestCompleted);
      if (t instanceof HttpRequestException) {
        HttpRequestException httpRequestException = (HttpRequestException) t;
        return sendError(httpRequestException.getStatus(), httpRequestException.getMessage());
      }
      throw t;
    }
    // All builds done, perform completion steps.
    IntStream.range(0, sessionIds.size())
        .forEach(i -> buildProvider.updateSession(sessionIds.get(i), buildIds.get(i)));
    
    List<Build> builds = buildProvider.getBuilds(buildIds);
    if (builds.size() == 0) {
      throw new RuntimeException("Couldn't fetch builds " + buildIds);
    }
  
    NewParallelBuildResponse res = new NewParallelBuildResponse().setBuildIds(buildIds);
    
    boolean isSuccess = builds.stream().
        allMatch(build -> build.getFinalStatus() == TestStatus.SUCCESS);
    
    TestStatus responseStatus = isSuccess ? TestStatus.SUCCESS : TestStatus.ERROR;
    res.setStatus(responseStatus.toString().toLowerCase(Locale.US));
  
    if (!(notifyOnComplete || buildRunConfig.isIncludeDetailedResultInResponse())) {
      return ResponseEntity.ok(res);
    }
    
    List<TestDetail> testDetails = testProvider.getAllCompletedTestDetail(buildIds);
  
    if (buildRunConfig.isIncludeDetailedResultInResponse()) {
      res.setTestDetails(testDetails);
    }
    
    if (notifyOnComplete) {
      parallelBuildCompletionEmailHandler.handle(buildRunConfig, builds, isSuccess, testDetails);
    }
    
    return ResponseEntity.ok(res);
  }
  
  @PostMapping("/projects/{projectId}/builds")
  @SuppressWarnings("unused")
  public ResponseEntity<?> newBuild(
      @Validated @RequestBody(required = false) BuildRunConfig buildRunConfig,
      @PathVariable @Min(1) int projectId,
      @RequestHeader(API_KEY_REQ_HEADER) String apiKey
  ) {
    if (buildRunConfig == null) {
      buildRunConfig = new BuildRunConfig();
    }
    UserDetail userDetail = common.verifyOrganizationProjectAndGetUserDetail(apiKey, projectId)
        .orElseThrow(() -> new UnauthorizedException("Either the API key is invalid or given" +
            " project wasn't found."));
    
    APIDefaults apiDefaults = apiDefaultsProvider.getApiDefaults(userDetail.getOrganizationId())
        .orElse(new APIDefaults());
    BuildConfig normalizedBuildConfig = deduceBuildConfig(buildRunConfig, apiDefaults);
    
    if (normalizedBuildConfig.getTotalParallel() > 1) {
      return newParallelBuild(userDetail, buildRunConfig, normalizedBuildConfig, projectId);
    }
    
    NewBuild newBuild = new NewBuild();
    newBuild.setBuildName(buildRunConfig.getBuildName());
    if (buildRunConfig.getWaitForCompletion() == null || buildRunConfig.getWaitForCompletion()) {
      newBuild.setSourceType(BuildSourceType.CI);
    } else {
      newBuild.setSourceType(BuildSourceType.NOT_IDE);
    }
    // Same bucket for all as this api is accessed org wide, and we don't want to set it to a
    // particular user.
    newBuild.setShotBucket(apiCoreProperties.getStorage().getShotBucketUsc());
    newBuild.setFiles(buildRunConfig.getFiles());
    newBuild.setBuildCapability(deduceBuildCaps(buildRunConfig));
    newBuild.setBuildConfig(normalizedBuildConfig);
    
    long buildRequestId = getNewBuildRequest(newBuild.getSourceType(), userDetail.getUserId());
    newBuild.setBuildRequestId(buildRequestId);
    try {
      int buildId = checkQuotaAndCreateNewBuild(newBuild, projectId, userDetail.getUserId());
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
        } catch (Throwable t) {
          LOG.error("Couldn't create buildVM", t);
          sessionFailureReason = SessionFailureReason.VM_NOT_CREATED;
          throw new HttpRequestException(HttpStatus.INTERNAL_SERVER_ERROR, VM_NOT_CREATED_ERROR);
        }
        // save buildVM details to vm table
        buildProvider.createAndUpdateVM(buildVM);
        // start new session as we've a VM
        String sessionId;
        try {
          sessionId = runnerService.newSession(buildVM);
        } catch (Throwable t) {
          LOG.error("Couldn't start a new session", t);
          sessionFailureReason = SessionFailureReason.NEW_SESSION_ERROR;
          throw new HttpRequestException(HttpStatus.INTERNAL_SERVER_ERROR, NEW_SESSION_ERROR);
        }
        // updates session_request_end_date
        buildProvider.updateSession(sessionId, buildId);
        
        NewBuildResponse res = new NewBuildResponse().setBuildId(buildId);
        if (newBuild.getSourceType() == BuildSourceType.CI) {
          List<Build> builds = buildProvider.getBuilds(Collections.singletonList(buildId));
          if (builds.size() == 0) {
            throw new RuntimeException("Couldn't fetch build " + buildId);
          }
          Build build = builds.get(0);
          res.setStatus(build.getFinalStatus().toString().toLowerCase(Locale.US));
          res.setError(build.getError());
          if (buildRunConfig.isIncludeDetailedResultInResponse()) {
            res.setTestDetails(testProvider.getAllCompletedTestDetail(
                Collections.singletonList(buildId)));
          }
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
  private int checkQuotaAndCreateNewBuild(NewBuild newBuild,
                                          int projectId,
                                          int userId) {
    try {
      validateNewBuildRequestQuota(userId, 0);
      // parallel, minutes quota verified, let's jump on create build.
      return buildProvider.createNewBuild(newBuild, projectId);
    } catch (Throwable t) {
      markBuildRequestCompleted(newBuild.getBuildRequestId());
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
  
  BuildConfig deduceBuildConfig(BuildRunConfig config, APIDefaults apiDefaults) {
    String disRes;
    int retryUpto = 0;
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
    
    if (configBConf.getRetryFailedTestsUpto() == null) {
      if (apiDefaults.getRetryFailedTestsUpto() != null) {
        retryUpto = apiDefaults.getRetryFailedTestsUpto();
      }
    } else {
      retryUpto = configBConf.getRetryFailedTestsUpto();
    }
    
    return new BuildConfig()
        .setTotalParallel(Math.max(configBConf.getTotalParallel(), 1))
        .setDisplayResolution(disRes)
        .setTimezone(configBConf.getTimezone() == null ? "UTC" : configBConf.getTimezone())
        .setCaptureShots(configBConf.isCaptureShots())
        .setCaptureDriverLogs(configBConf.isCaptureDriverLogs())
        .setRetryFailedTestsUpto(retryUpto)
        .setNotifyOnCompletion(configBConf.getNotifyOnCompletion())
        .setBuildVars(configBConf.getBuildVars());
  }
  
  private long getNewBuildRequest(BuildSourceType sourceType, int userId) {
    return buildRequestProvider.newBuildRequest(new BuildRequest()
        .setTotalParallel(1)
        .setBuildSourceType(sourceType).setUserId(userId));
  }
  
  private void validateNewBuildRequestQuota(int userId, int currentParallelNotYetInBuildRequest) {
    UsersPlan usersPlan = userPlanProvider.getUserPlan(userId)
        .orElseThrow(() -> new RuntimeException(userId + " User plan not found"));
    List<BuildRequest> buildRequests = buildRequestProvider.getCurrentBuildRequests(userId);
    int totalBuildRequests = buildRequests.size();
    
    if (totalBuildRequests + currentParallelNotYetInBuildRequest > usersPlan.getTotalParallel()) {
      throw new HttpRequestException(HttpStatus.TOO_MANY_REQUESTS,
          "Total parallel builds limit reached." +
              " If you're starting several builds together, please slow down and wait for a few" +
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
