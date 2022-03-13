package com.zylitics.api.dao;

import com.google.common.base.Preconditions;
import com.zylitics.api.model.*;
import com.zylitics.api.provider.*;
import com.zylitics.api.util.CommonUtil;
import com.zylitics.api.util.DateTimeUtil;
import com.zylitics.api.util.Randoms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class DaoBuildProvider extends AbstractDaoProvider implements BuildProvider {
  
  private final TransactionTemplate transactionTemplate;
  
  private final Randoms randoms;
  
  private final BuildCapabilityProvider buildCapabilityProvider;
  
  private final TestProvider testProvider;
  
  private final BuildVarProvider buildVarProvider;
  
  private final GlobalVarProvider globalVarProvider;
  
  private final BuildVMProvider buildVMProvider;
  
  @Autowired
  DaoBuildProvider(NamedParameterJdbcTemplate jdbc,
                   TransactionTemplate transactionTemplate,
                   Randoms randoms,
                   BuildCapabilityProvider buildCapabilityProvider,
                   TestProvider testProvider,
                   BuildVarProvider buildVarProvider,
                   GlobalVarProvider globalVarProvider,
                   BuildVMProvider buildVMProvider) {
    super(jdbc);
    this.transactionTemplate = transactionTemplate;
    this.randoms = randoms;
    this.buildCapabilityProvider = buildCapabilityProvider;
    this.testProvider = testProvider;
    this.buildVarProvider = buildVarProvider;
    this.globalVarProvider = globalVarProvider;
    this.buildVMProvider = buildVMProvider;
  }
  
  @Override
  public Optional<Build> getBuild(int buildId) {
    String sql = "SELECT build_key, name, bt_build_vm_id, server_screen_size,\n" +
        "server_timezone_with_dst, session_key,\n" +
        "session_request_start_date AT TIME ZONE 'UTC' AS session_request_start_date,\n" +
        "session_request_end_date AT TIME ZONE 'UTC' AS session_request_end_date,\n" +
        "session_failure_reason,\n" +
        "start_date AT TIME ZONE 'UTC' AS start_date,\n" +
        "end_date AT TIME ZONE 'UTC' AS end_date,\n" +
        "all_done_date AT TIME ZONE 'UTC' AS all_done_date,\n" +
        "final_status, error, shot_bucket_session_storage, abort_on_failure,\n" +
        "aet_keep_single_window, aet_update_url_blank, aet_reset_timeouts,\n" +
        "aet_delete_all_cookies, bt_project_id, source_type, bt_build_request_id,\n" +
        "create_date AT TIME ZONE 'UTC' AS create_date\n" +
        "FROM bt_build\n" +
        "WHERE bt_build_id = :bt_build_id";
    List<Build> builds = jdbc.query(sql, new SqlParamsBuilder()
        .withInteger("bt_build_id", buildId).build(), (rs, rowNum) ->
        new Build()
            .setBuildId(buildId)
            .setBuildKey(rs.getString("build_key"))
            .setName(rs.getString("name"))
            .setBuildVMId(CommonUtil.getIntegerSqlVal(rs, "bt_build_vm_id"))
            .setServerScreenSize(rs.getString("server_screen_size"))
            .setServerTimezone(rs.getString("server_timezone_with_dst"))
            .setSessionKey(rs.getString("session_key"))
            .setSessionRequestStartDate(
                CommonUtil.getEpochSecsOrNullFromSqlTimestamp(rs, "session_request_start_date"))
            .setSessionRequestEndDate(
                CommonUtil.getEpochSecsOrNullFromSqlTimestamp(rs, "session_request_end_date"))
            .setSessionFailureReason(CommonUtil.convertEnumFromSqlVal(rs,
                "session_failure_reason", SessionFailureReason.class))
            .setStartDate(
                CommonUtil.getEpochSecsOrNullFromSqlTimestamp(rs, "start_date"))
            .setEndDate(
                CommonUtil.getEpochSecsOrNullFromSqlTimestamp(rs, "end_date"))
            .setAllDoneDate(
                CommonUtil.getEpochSecsOrNullFromSqlTimestamp(rs, "all_done_date"))
            .setFinalStatus(CommonUtil.convertEnumFromSqlVal(rs,
                "final_status", TestStatus.class))
            .setError(rs.getString("error"))
            .setShotBucketSessionStorage(rs.getString("shot_bucket_session_storage"))
            .setAbortOnFailure(rs.getBoolean("abort_on_failure"))
            .setAetKeepSingleWindow(rs.getBoolean("aet_keep_single_window"))
            .setAetUpdateUrlBlank(rs.getBoolean("aet_update_url_blank"))
            .setAetResetTimeouts(rs.getBoolean("aet_reset_timeouts"))
            .setAetDeleteAllCookies(rs.getBoolean("aet_delete_all_cookies"))
            .setProjectId(rs.getInt("bt_project_id"))
            .setSourceType(BuildSourceType.valueOf(rs.getString("source_type")))
            .setBuildRequestId(rs.getLong("bt_build_request_id"))
            .setCreateDate(CommonUtil.getEpochSecsFromSqlTimestamp(rs, "create_date")));
    if (builds.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(builds.get(0));
  }
  
  @Override
  public List<Integer> createNewBuilds(List<NewBuild> newBuilds,
                                       @Nullable List<IncomingFile> incomingFiles,
                                       int projectId,
                                       String insufficientTestsExMsg) {
    List<Integer> newBuildIds = transactionTemplate.execute(ts -> createNewBuildsInTransaction(
        newBuilds, incomingFiles, projectId, insufficientTestsExMsg));
    Objects.requireNonNull(newBuildIds);
    Preconditions.checkArgument(newBuilds.size() == newBuildIds.size());
    return newBuildIds;
  }
  
  @Override
  public int createNewBuild(NewBuild newBuild,
                            int projectId) {
    Integer newBuildId = transactionTemplate.execute(ts -> createNewBuildInTransaction(
        newBuild, projectId));
    Objects.requireNonNull(newBuildId);
    return newBuildId;
  }
  
  private String getBuildKey() {
    // TODO: currently I'm generating and putting a random without checking for existence, it may
    //  fail when duplicate. Keep and eye and fix later.
    return randoms.generateRandom(10);
  }
  
  private List<Integer> createNewBuildsInTransaction(List<NewBuild> newBuilds,
                                                     @Nullable List<IncomingFile> incomingFiles,
                                                     int projectId,
                                                     String insufficientTestsExMsg) {
    List<Integer> newBuildIds = new ArrayList<>();
    
    newBuilds.forEach(newBuild -> {
      int buildId = createBuildRecord(newBuild, projectId);
      newBuildIds.add(buildId);
      
      buildCapabilityProvider.captureCapability(newBuild.getBuildCapability(), buildId);
  
      buildVarProvider.capturePrimaryBuildVarsOverridingGiven(projectId,
          newBuild.getBuildConfig().getBuildVars(),
          buildId);
  
      globalVarProvider.captureGlobalVars(projectId, buildId);
    });
  
    testProvider.splitAndCaptureTests(newBuildIds, incomingFiles, projectId,
        insufficientTestsExMsg);
  
    return newBuildIds;
  }
  
  private int createBuildRecord(NewBuild newBuild, int projectId) {
    String sql = "INSERT INTO bt_build\n" +
        "(build_key, name, server_screen_size, server_timezone_with_dst,\n" +
        "shot_bucket_session_storage, abort_on_failure, retryFailedTestsUpto,\n" +
        "capture_shots, capture_driver_logs, notify_on_completion,\n" +
        "aet_keep_single_window, aet_update_url_blank, aet_reset_timeouts,\n" +
        "aet_delete_all_cookies, bt_project_id, source_type, bt_build_request_id, create_date)\n" +
        "VALUES (:build_key, :name, :server_screen_size, :server_timezone_with_dst,\n" +
        ":shot_bucket_session_storage, :abort_on_failure, :retryFailedTestsUpto,\n" +
        ":capture_shots, :capture_driver_logs, :notify_on_completion,\n" +
        ":aet_keep_single_window,\n" +
        ":aet_update_url_blank, :aet_reset_timeouts, :aet_delete_all_cookies, :bt_project_id,\n" +
        ":source_type, :bt_build_request_id, :create_date) RETURNING bt_build_id";
    BuildConfig config = newBuild.getBuildConfig();
    return jdbc.query(sql, new SqlParamsBuilder()
        .withVarchar("build_key", getBuildKey())
        .withOther("name", newBuild.getBuildName())
        .withVarchar("server_screen_size", config.getDisplayResolution())
        .withOther("server_timezone_with_dst", config.getTimezone())
        .withVarchar("shot_bucket_session_storage", newBuild.getShotBucket())
        .withBoolean("abort_on_failure", false)
        .withInteger("retryFailedTestsUpto", config.getRetryFailedTestsUpto())
        .withBoolean("capture_shots", config.isCaptureShots() == null || config.isCaptureShots()) // when not sent, it's true
        .withBoolean("capture_driver_logs", config.isCaptureDriverLogs())
        .withBoolean("notify_on_completion", config.getNotifyOnCompletion())
        .withBoolean("aet_keep_single_window", true)
        .withBoolean("aet_update_url_blank", true)
        .withBoolean("aet_reset_timeouts", true)
        .withBoolean("aet_delete_all_cookies", true)
        .withOther("source_type", newBuild.getSourceType())
        .withBigint("bt_build_request_id", newBuild.getBuildRequestId())
        .withProject(projectId)
        .withCreateDate().build(), CommonUtil.getSingleInt()).get(0);
  }
  
  private int createNewBuildInTransaction(NewBuild newBuild,
                                          int projectId) {
    int buildId = createBuildRecord(newBuild, projectId);
    
    BuildConfig config = newBuild.getBuildConfig();
    
    testProvider.captureTests(newBuild.getFiles(), projectId, buildId);
  
    buildCapabilityProvider.captureCapability(newBuild.getBuildCapability(), buildId);
    
    buildVarProvider.capturePrimaryBuildVarsOverridingGiven(projectId,
        config.getBuildVars(),
        buildId);
    
    globalVarProvider.captureGlobalVars(projectId, buildId);
    
    return buildId;
  }
  
  @Override
  public void createAndUpdateVM(BuildVM buildVM) {
    transactionTemplate.executeWithoutResult(ts -> {
      int buildVMId = buildVMProvider.newBuildVM(buildVM);
      String sql = "UPDATE bt_build SET bt_build_vm_id = :bt_build_vm_id\n" +
          "WHERE bt_build_id = :bt_build_id";
      CommonUtil.validateSingleRowDbCommit(jdbc.update(sql, new SqlParamsBuilder()
          .withInteger("bt_build_vm_id", buildVMId)
          .withInteger("bt_build_id", buildVM.getBuildId()).build()));
    });
  }
  
  @Override
  public void updateSessionRequestStart(int buildId) {
    String sql = "UPDATE bt_build SET session_request_start_date = :session_request_start_date\n" +
        "WHERE bt_build_id = :bt_build_id";
    CommonUtil.validateSingleRowDbCommit(jdbc.update(sql, new SqlParamsBuilder()
        .withTimestampTimezone("session_request_start_date", DateTimeUtil.getCurrentUTC())
        .withInteger("bt_build_id", buildId).build()));
  }
  
  @Override
  public void updateSession(String sessionId, int buildId) {
    String sql = "UPDATE bt_build SET session_key = :session_key,\n" +
        "session_request_end_date = :session_request_end_date\n" +
        "WHERE bt_build_id = :bt_build_id";
    CommonUtil.validateSingleRowDbCommit(jdbc.update(sql, new SqlParamsBuilder()
        .withVarchar("session_key", sessionId)
        .withTimestampTimezone("session_request_end_date", DateTimeUtil.getCurrentUTC())
        .withInteger("bt_build_id", buildId).build()));
  }
  
  @Override
  public void updateOnSessionFailure(SessionFailureReason sessionFailureReason,
                                     String error,
                                     int buildId) {
    String sql = "UPDATE bt_build SET session_failure_reason = :session_failure_reason,\n" +
        "error = :error, session_request_end_date = :session_request_end_date\n" +
        "WHERE bt_build_id = :bt_build_id";
    CommonUtil.validateSingleRowDbCommit(jdbc.update(sql, new SqlParamsBuilder()
        .withOther("session_failure_reason", sessionFailureReason)
        .withOther("error", error)
        .withTimestampTimezone("session_request_end_date", DateTimeUtil.getCurrentUTC())
        .withInteger("bt_build_id", buildId).build()));
  }
}
