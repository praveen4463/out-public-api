package com.zylitics.api.provider;

import com.zylitics.api.model.*;

import java.util.Optional;

public interface BuildProvider {
  
  Optional<Build> getBuild(int buildId);
  
  int newBuild(NewBuild newBuild, long buildRequestId, int projectId);
  
  void createAndUpdateVM(BuildVM buildVM, int buildId);
  
  void updateSessionRequestStart(int buildId);
  
  void updateSession(String sessionId, int buildId);
  
  void updateOnSessionFailure(SessionFailureReason sessionFailureReason, String error,
                              int buildId);
}
