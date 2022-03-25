package com.zylitics.api.model;

import java.util.List;

public class NewParallelBuildResponse extends AbstractBuildResponse {
  
  private List<Integer> buildIds;
  
  public List<Integer> getBuildIds() {
    return buildIds;
  }
  
  public NewParallelBuildResponse setBuildIds(List<Integer> buildIds) {
    this.buildIds = buildIds;
    return this;
  }
}
