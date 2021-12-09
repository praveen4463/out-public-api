package com.zylitics.api.controllers;

import com.zylitics.api.model.BuildVM;
import com.zylitics.api.model.NewBuildVM;

public interface VMService {
  
  BuildVM newBuildVM(NewBuildVM newBuildVM);
}
