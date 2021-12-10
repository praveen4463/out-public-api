package com.zylitics.api.provider;

import com.zylitics.api.model.UsersPlan;

import java.util.Optional;

public interface UserPlanProvider {
  
  Optional<UsersPlan> getUserPlan(int userId);
}
