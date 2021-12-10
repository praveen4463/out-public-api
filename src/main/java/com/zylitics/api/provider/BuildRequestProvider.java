package com.zylitics.api.provider;

import com.zylitics.api.model.BuildRequest;

import java.util.List;

public interface BuildRequestProvider {

  long newBuildRequest(BuildRequest buildRequest);
  
  List<BuildRequest> getCurrentBuildRequests(int userId);
  
  void markBuildRequestCompleted(long buildRequestId);
}
