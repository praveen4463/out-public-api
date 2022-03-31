package com.zylitics.api.services;

import com.zylitics.api.provider.BuildProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Service
public class BuildCompletionCheckerService {
  
  private static final long TIMEOUT_MILLIS = 10 * 60 * 60 * 1000; // 10h
  private static final long POLL_INTERVAL_MILLIS = 30 * 1000;
  
  private final BuildProvider buildProvider;
  
  public BuildCompletionCheckerService(BuildProvider buildProvider) {
    this.buildProvider = buildProvider;
  }
  
  public void wait(List<Integer> buildIds) {
    long start = System.currentTimeMillis();
    try {
      while (true) {
        if (buildProvider.haveBuildsCompleted(buildIds)) {
          return;
        }
        long elapsed = System.currentTimeMillis() - start;
        if (elapsed > TIMEOUT_MILLIS) {
          throw new TimeoutException(String.format("Couldn't check completion of builds %s after waiting for %d",
              buildIds, elapsed));
        }
        MILLISECONDS.sleep(POLL_INTERVAL_MILLIS);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
