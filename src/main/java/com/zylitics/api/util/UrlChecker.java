package com.zylitics.api.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class UrlChecker {
  
  private static final int CONNECT_TIMEOUT_MS = 500;
  private static final int READ_TIMEOUT_MS = 1000;
  private static final long MAX_POLL_INTERVAL_MS = 1000;
  private static final long MIN_POLL_INTERVAL_MS = 100;
  
  public void waitUntilAvailable(long timeout, TimeUnit unit, String url) {
    long start = System.nanoTime();
    try {
      HttpURLConnection connection = null;
      long sleepMillis = MIN_POLL_INTERVAL_MS;
      long maxTimeout = MILLISECONDS.convert(timeout, unit);
      while (true) {
        try {
          connection = connectToUrl(new URL(url));
          if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return;
          }
        } catch (IOException e) {
          // check again.
        } finally {
          if (connection != null) {
            connection.disconnect();
          }
        }
        long elapsed = MILLISECONDS.convert(System.nanoTime() - start, NANOSECONDS);
        if (elapsed > maxTimeout) {
          throw new TimeoutException(String.format("Couldn't connect to %s after waiting for %d",
              url, elapsed));
        }
        MILLISECONDS.sleep(sleepMillis);
        sleepMillis = (sleepMillis >= MAX_POLL_INTERVAL_MS) ? sleepMillis : sleepMillis * 2;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  private HttpURLConnection connectToUrl(URL url) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
    connection.setReadTimeout(READ_TIMEOUT_MS);
    connection.connect();
    return connection;
  }
}
