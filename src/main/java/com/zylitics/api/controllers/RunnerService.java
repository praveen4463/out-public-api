package com.zylitics.api.controllers;

import com.zylitics.api.model.BuildVM;

import java.util.List;

public interface RunnerService {
  
  String newSession(BuildVM buildVM);
  
  List<String> newSessions(List<BuildVM> buildVMs);
}
