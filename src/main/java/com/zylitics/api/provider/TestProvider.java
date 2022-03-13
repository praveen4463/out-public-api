package com.zylitics.api.provider;

import com.zylitics.api.model.IncomingFile;
import com.zylitics.api.model.TestDetail;

import javax.annotation.Nullable;
import java.util.List;

public interface TestProvider {
  
  /*
  Splits the available tests among the provided buildIds equally and captures them.
   */
  void splitAndCaptureTests(List<Integer> buildIds,
                            @Nullable List<IncomingFile> incomingFiles,
                            int projectId,
                            String insufficientTestsExMsg) throws IllegalArgumentException;
  
  void captureTests(@Nullable List<IncomingFile> incomingFiles, int projectId, int buildId);
  
  List<TestDetail> getAllCompletedTestDetail(int buildId);
}
