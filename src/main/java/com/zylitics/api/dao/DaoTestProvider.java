package com.zylitics.api.dao;

import com.google.cloud.Tuple;
import com.google.common.base.Preconditions;
import com.zylitics.api.model.IncomingFile;
import com.zylitics.api.model.IncomingTest;
import com.zylitics.api.model.TestDetail;
import com.zylitics.api.model.TestStatus;
import com.zylitics.api.provider.TestProvider;
import com.zylitics.api.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.sql.JDBCType;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DaoTestProvider extends AbstractDaoProvider implements TestProvider {
  
  private static final String TEST_Q_INSERT =
      "INSERT INTO bt_build_tests (bt_build_id, bt_file_id, bt_file_name,\n" +
      "bt_test_id, bt_test_name, bt_test_version_id, bt_test_version_name,\n" +
      "bt_test_version_code, bt_test_version_code_lines)\n";
  
  @Autowired
  DaoTestProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public void splitAndCaptureTests(List<Integer> buildIds,
                                   @Nullable List<IncomingFile> incomingFiles,
                                   int projectId,
                                   String insufficientTestsExMsg) {
    int totalBuilds = buildIds.size();
    Preconditions.checkArgument(totalBuilds > 0);
    
    if (totalBuilds == 1) {
      captureTests(incomingFiles, projectId, buildIds.get(0));
      return;
    }
    
    Tuple<String, SqlParamsBuilder> queryAndParams = getTestQuerySelectStmOrderedAndParams(
        incomingFiles,
        projectId);
    String selectStmOrdered = queryAndParams.x();
    SqlParamsBuilder paramsBuilder = queryAndParams.y();
    int totalTests = jdbc.query("SELECT count(*) FROM (\n" + selectStmOrdered + "\n) t",
        paramsBuilder.build(),
        CommonUtil.getSingleInt()).get(0);
    if (totalBuilds > totalTests) {
      throw new IllegalArgumentException(insufficientTestsExMsg);
    }
    int blockSize = Math.floorDiv(totalTests, totalBuilds);
    
    StringBuilder compositeSelect = new StringBuilder();
    for (int i = 0; i < totalBuilds; i++) {
      boolean notLast = i < totalBuilds - 1;
      String sql = selectStmOrdered.replace(":bt_build_id", ":bt_build_id" + (i + 1)) +
          (notLast ? "LIMIT " + blockSize : "") + "\n" + // Allocate remaining tests for last build
          "OFFSET " + (i * blockSize) + "\n";
      paramsBuilder.withInteger("bt_build_id" + (i + 1), buildIds.get(i));
      // Keep select statement in parentheses because the LIMIT and OFFSET operators can't be mixed with UNION
      compositeSelect.append("(");
      compositeSelect.append(sql);
      compositeSelect.append(")");
      if (notLast) {
        compositeSelect.append("UNION ALL\n");
      }
    }
    jdbc.update(TEST_Q_INSERT + compositeSelect, paramsBuilder.build());
  }
  
  private Tuple<String, SqlParamsBuilder> getTestQuerySelectStmOrderedAndParams(
      @Nullable List<IncomingFile> incomingFiles,
      int projectId) {
    return getTestQuerySelectStmOrderedAndParams(incomingFiles, projectId, 0);
  }
  
  private Tuple<String, SqlParamsBuilder> getTestQuerySelectStmOrderedAndParams(
      @Nullable List<IncomingFile> incomingFiles,
      int projectId,
      int buildId) {
    String baseSql = "SELECT :bt_build_id, f.bt_file_id, f.name, t.bt_test_id, t.name,\n" +
        "v.bt_test_version_id, v.name, v.code,\n" +
        "(SELECT count(*) + 1 FROM (SELECT regexp_matches(v.code, '\\n', 'g')) t) code_lines\n" +
        "FROM bt_file f\n" +
        "JOIN bt_test t USING (bt_file_id)\n" +
        "JOIN bt_test_version v USING (bt_test_id)\n" +
        "WHERE bt_project_id = :bt_project_id\n" +
        "AND code IS NOT NULL\n" +
        "AND t.name !~ '^[a-z0-9_]+$'\n" + // Match only non identifiers test names. TODO: Remove this once we've functions
        "AND length(regexp_replace(coalesce(code, ''), '[\\n\\r\\t\\s]', '', 'g')) > 0\n";
    String orderByStm = "ORDER BY f.name, t.name\n";
  
    SqlParamsBuilder paramsBuilder = new SqlParamsBuilder();
    paramsBuilder.withInteger("bt_build_id", buildId);
    paramsBuilder.withInteger("bt_project_id", projectId);
  
    StringBuilder filesSelector = new StringBuilder();
    int paramUniqueCounter = 0;
  
    if (incomingFiles != null) {
      List<IncomingFile> withoutVersionList = incomingFiles.stream()
          .filter(f -> f.getTests() == null || f.getTests().stream()
              .anyMatch(t -> t.getVersions() == null)).collect(Collectors.toList());
      Iterator<IncomingFile> withoutVersion = withoutVersionList.iterator();
      while (withoutVersion.hasNext()) {
        ++paramUniqueCounter;
        IncomingFile incomingFile = withoutVersion.next();
        filesSelector.append("(");
        filesSelector.append(baseSql);
        filesSelector.append(String.format("AND f.name = :file_name%s\n", paramUniqueCounter));
        paramsBuilder.withVarchar("file_name" + paramUniqueCounter, incomingFile.getName());
      
        if (incomingFile.getTests() != null && incomingFile.getTests().size() > 0) {
          filesSelector.append(String.format(
              "AND t.name in (SELECT * FROM unnest(:test_names%s))\n", paramUniqueCounter));
          paramsBuilder.withArray("test_names" + paramUniqueCounter,
              incomingFile.getTests().stream()
                  .filter(t -> t.getVersions() == null)
                  .map(IncomingTest::getName).toArray(),
              JDBCType.VARCHAR);
        }
      
        filesSelector.append("AND is_current = true\n");
        filesSelector.append(orderByStm);
        filesSelector.append(")");
      
        if (withoutVersion.hasNext()) {
          filesSelector.append("UNION ALL\n");
        }
      }
    
      Iterator<IncomingFile> withVersion = incomingFiles.stream()
          .filter(f -> f.getTests() != null && f.getTests().stream()
              .anyMatch(t -> t.getVersions() != null)).collect(Collectors.toList()).iterator();
    
      // if we've more to select and have already selected, add a union all.
      if (withVersion.hasNext() && withoutVersionList.size() > 0) {
        filesSelector.append("UNION ALL\n");
      }
    
      while (withVersion.hasNext()) {
        ++paramUniqueCounter;
        IncomingFile incomingFile = withVersion.next();
        //noinspection ConstantConditions
        for (IncomingTest incomingTest : incomingFile.getTests().stream()
            .filter(t -> t.getVersions() != null).collect(Collectors.toList())) {
          filesSelector.append("(");
          filesSelector.append(baseSql);
          filesSelector.append(String.format("AND f.name = :file_name%s\n", paramUniqueCounter));
          paramsBuilder.withVarchar("file_name" + paramUniqueCounter, incomingFile.getName());
        
          filesSelector.append(String.format("AND t.name = :test_name%s\n", paramUniqueCounter));
          paramsBuilder.withVarchar("test_name" + paramUniqueCounter, incomingTest.getName());
        
          filesSelector.append(String.format(
              "AND v.name in (SELECT * FROM unnest(:version_names%s))\n", paramUniqueCounter));
          //noinspection ConstantConditions
          paramsBuilder.withArray("version_names" + paramUniqueCounter,
              incomingTest.getVersions().toArray(), JDBCType.VARCHAR);
          filesSelector.append(orderByStm);
          filesSelector.append(")");
        
          if (withVersion.hasNext()) {
            filesSelector.append("UNION ALL\n");
          }
        }
      }
    } else {
      filesSelector.append(baseSql);
      filesSelector.append("AND is_current = true\n");
      filesSelector.append(orderByStm);
    }
    return Tuple.of(filesSelector.toString(), paramsBuilder);
  }
  
  @Override
  public void captureTests(@Nullable List<IncomingFile> incomingFiles, int projectId, int buildId) {
    Tuple<String, SqlParamsBuilder> queryAndParams = getTestQuerySelectStmOrderedAndParams(incomingFiles,
        projectId, buildId);
    
    int result = jdbc.update(TEST_Q_INSERT + queryAndParams.x(),
        queryAndParams.y().build());
    if (result < 1) {
      throw new RuntimeException("No tests found for running. " +
          "Either the given tests don't exist or they've empty code.");
    }
  }
  
  @Override
  public List<TestDetail> getAllCompletedTestDetail(List<Integer> buildIds) {
    String sql = "SELECT bt_build_id, bt_test_version_name, bt_file_name, bt_test_name, status,\n" +
        "error, url_upon_error, end_date AT TIME ZONE 'UTC' AS end_date\n" +
        "FROM bt_build_tests JOIN bt_build_status USING (bt_build_id, bt_test_version_id)\n" +
        "WHERE bt_build_id in (SELECT * FROM unnest(:build_ids)) ORDER BY bt_build_tests_id";
    return jdbc.query(sql,
        new SqlParamsBuilder().withArray("build_ids", buildIds.toArray(), JDBCType.INTEGER)
            .build(), (rs, rowNum) ->
            new TestDetail()
                .setBuildId(rs.getInt("bt_build_id"))
                .setVersion(rs.getString("bt_test_version_name"))
                .setStatus(CommonUtil.convertEnumFromSqlVal(rs, "status", TestStatus.class))
                .setFile(rs.getString("bt_file_name"))
                .setTest(rs.getString("bt_test_name"))
                .setError(rs.getString("error"))
                .setUrlUponError(rs.getString("url_upon_error"))
                .setEndDate(CommonUtil.getDateTimeOrNullFromSqlTimestamp(rs, "end_date")));
  }
}
