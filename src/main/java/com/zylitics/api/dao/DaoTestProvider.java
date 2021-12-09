package com.zylitics.api.dao;

import com.zylitics.api.model.IncomingFile;
import com.zylitics.api.model.IncomingTest;
import com.zylitics.api.provider.TestProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.JDBCType;
import java.util.List;
import java.util.stream.Collectors;

public class DaoTestProvider extends AbstractDaoProvider implements TestProvider {
  
  @Autowired
  DaoTestProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public void captureTests(List<IncomingFile> incomingFiles, int buildId) {
    String insertSql = "INSERT INTO bt_build_tests (bt_build_id, bt_file_id, bt_file_name,\n" +
        "bt_test_id, bt_test_name, bt_test_version_id, bt_test_version_name,\n" +
        "bt_test_version_code, bt_test_version_code_lines)\n";
    String selectSql = "SELECT :bt_build_id, f.bt_file_id, f.name, t.bt_test_id, t.name,\n" +
        "v.bt_test_version_id, v.name, v.code,\n" +
        "(SELECT count(*) + 1 FROM (SELECT regexp_matches(v.code, '\\n', 'g')) t) code_lines\n" +
        "FROM bt_file f\n" +
        "JOIN bt_test t USING (bt_file_id)\n" +
        "JOIN bt_test_version v USING (bt_test_id)\n";
    String defaultAndCondition = "AND code IS NOT NULL\n" +
        "AND length(regexp_replace(coalesce(code, ''), '[\\n\\r\\t\\s]', '', 'g')) > 0\n";
    String fileNameCondition = "f.name = :file_name\n";
    String whereClause = "WHERE\n";
    
    SqlParamsBuilder paramsBuilder = new SqlParamsBuilder();
    paramsBuilder.withInteger("bt_build_id", buildId);
    
    StringBuilder filesSelector = new StringBuilder();
    
    for (IncomingFile incomingFile : incomingFiles.stream()
        .filter(f -> f.getTests() == null || f.getTests().stream()
            .anyMatch(t -> t.getVersions() == null)).collect(Collectors.toList())) {
      filesSelector.append(selectSql);
      filesSelector.append(whereClause);
      filesSelector.append(fileNameCondition);
      paramsBuilder.withVarchar("file_name", incomingFile.getName());
      
      if (incomingFile.getTests() != null && incomingFile.getTests().size() > 0) {
        filesSelector.append("AND t.name in (SELECT * FROM unnest(:test_names))\n");
        paramsBuilder.withArray("test_names",
            incomingFile.getTests().stream()
                .filter(t -> t.getVersions() == null)
                .map(IncomingTest::getName).toArray(),
            JDBCType.VARCHAR);
      }
      
      filesSelector.append("AND is_current = true\n");
      
      filesSelector.append(defaultAndCondition);
      
      filesSelector.append("UNION ALL\n");
    }
  
    for (IncomingFile incomingFile : incomingFiles.stream()
        .filter(f -> f.getTests() != null && f.getTests().stream()
            .anyMatch(t -> t.getVersions() != null)).collect(Collectors.toList())) {
      //noinspection ConstantConditions
      for (IncomingTest incomingTest : incomingFile.getTests().stream()
          .filter(t -> t.getVersions() != null).collect(Collectors.toList())) {
        filesSelector.append(selectSql);
        filesSelector.append(whereClause);
        filesSelector.append(fileNameCondition);
        paramsBuilder.withVarchar("file_name", incomingFile.getName());
  
        filesSelector.append("AND t.name = :test_name\n");
        paramsBuilder.withVarchar("test_name", incomingTest.getName());
  
        filesSelector.append("AND v.name in (SELECT * FROM unnest(:version_names))\n");
        //noinspection ConstantConditions
        paramsBuilder.withArray("version_names",
            incomingTest.getVersions().toArray(), JDBCType.VARCHAR);
  
        filesSelector.append(defaultAndCondition);
  
        filesSelector.append("UNION ALL\n");
      }
    }
  
    int result = jdbc.update(insertSql + filesSelector, paramsBuilder.build());
    if (result < 1) {
      throw new RuntimeException("No tests found for running. " +
          "Either the given tests don't exist or they've empty code.");
    }
  }
}
