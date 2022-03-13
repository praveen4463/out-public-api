package com.zylitics.api.controllers;

import com.zylitics.api.model.BuildVM;
import com.zylitics.api.model.NewBuildVM;

import java.util.List;

public interface VMService {
  
  BuildVM newBuildVM(NewBuildVM newBuildVM);
  
  List<BuildVM> newBuildVMs(List<NewBuildVM> newBuildVMs);
}
