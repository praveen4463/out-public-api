package com.zylitics.api.provider;

import com.zylitics.api.model.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public interface BuildProvider {
  
  Optional<Build> getBuild(int buildId);
  
  int createNewBuild(NewBuild newBuild, int projectId);
  
  List<Integer> createNewBuilds(List<NewBuild> newBuilds,
                                @Nullable List<IncomingFile> incomingFiles,
                                int projectId,
                                String insufficientTestsExMsg);
  
  void createAndUpdateVM(BuildVM buildVM);
  
  void updateSessionRequestStart(int buildId);
  
  void updateSession(String sessionId, int buildId);
  
  void updateOnSessionFailure(SessionFailureReason sessionFailureReason, String error,
                              int buildId);
}
