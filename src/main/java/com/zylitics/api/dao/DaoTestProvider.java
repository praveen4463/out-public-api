package com.zylitics.api.dao;

import com.zylitics.api.model.IncomingFile;
import com.zylitics.api.model.IncomingTest;
import com.zylitics.api.provider.TestProvider;
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
  
  @Autowired
  DaoTestProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public void captureTests(@Nullable List<IncomingFile> incomingFiles, int projectId, int buildId) {
    String insertSql = "INSERT INTO bt_build_tests (bt_build_id, bt_file_id, bt_file_name,\n" +
        "bt_test_id, bt_test_name, bt_test_version_id, bt_test_version_name,\n" +
        "bt_test_version_code, bt_test_version_code_lines)\n";
    
    String baseSql = "SELECT :bt_build_id, f.bt_file_id, f.name, t.bt_test_id, t.name,\n" +
        "v.bt_test_version_id, v.name, v.code,\n" +
        "(SELECT count(*) + 1 FROM (SELECT regexp_matches(v.code, '\\n', 'g')) t) code_lines\n" +
        "FROM bt_file f\n" +
        "JOIN bt_test t USING (bt_file_id)\n" +
        "JOIN bt_test_version v USING (bt_test_id)\n" +
        "WHERE bt_project_id = :bt_project_id\n" +
        "AND code IS NOT NULL\n" +
        "AND length(regexp_replace(coalesce(code, ''), '[\\n\\r\\t\\s]', '', 'g')) > 0\n";
    
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
  
          if (withVersion.hasNext()) {
            filesSelector.append("UNION ALL\n");
          }
        }
      }
    } else {
      filesSelector.append(baseSql);
      filesSelector.append("AND is_current = true\n");
    }
    
    int result = jdbc.update(insertSql + filesSelector, paramsBuilder.build());
    if (result < 1) {
      throw new RuntimeException("No tests found for running. " +
          "Either the given tests don't exist or they've empty code.");
    }
  }
}
