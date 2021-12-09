package com.zylitics.api.controllers;

public interface RunnerService {
  
  String newSession(String runnerIP, int buildId);
  
  boolean stopBuild(String runnerIP, int buildId);
}
