package com.zylitics.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zylitics.api.controllers.RunnerService;
import com.zylitics.api.controllers.VMService;
import com.zylitics.api.config.APICoreProperties;
import com.zylitics.api.services.LocalRunnerService;
import com.zylitics.api.services.LocalVMService;
import com.zylitics.api.services.ProductionRunnerService;
import com.zylitics.api.services.ProductionVMService;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

// TODO: I am not too sure what DataAccessExceptions should be re-tried, let's first watch logs and
//  decide if retry can help recovering from them. Hikari automatically retries until connection
//  timeout so probably we could retry on lock failure, deadlock etc. Any code that invokes methods
//  on NamedParameterJdbcTemplate or JdbcTemplate can throw subclasses of this exception.
//  Perhaps the best way to do it would be to extend NamedParameterJdbcTemplate and the methods
//  we're using. Detect errors there, reattempt if necessary and throw if failed.
//  https://docs.spring.io/spring/docs/current/spring-framework-reference/data-access.html#dao-exceptions
@SpringBootApplication
public class Launcher {
  
  private static final String USER_INFO_REQ_HEADER = "X-Endpoint-API-UserInfo";
  
  private static final String FIREBASE_SERVICE_ACCOUNT_KEY = "FIREBASE_SA";
  
  private static final String FIREBASE_LOCAL_SERVICE_ACCOUNT_KEY = "FIREBASE_SA_LOCAL";
  
  public static void main(String[] args) {
    SpringApplication.run(Launcher.class, args);
  }
  
  @Bean
  @Profile({"production", "e2e"})
  Storage storage() {
    return StorageOptions.getDefaultInstance().getService();
  }
  
  // https://github.com/brettwooldridge/HikariCP
  // https://github.com/pgjdbc/pgjdbc#connection-properties
  // Boot won't autoconfigure DataSource if a bean is already declared.
  @Bean
  @Profile("production")
  DataSource hikariDataSource(APICoreProperties apiCoreProperties, SecretsManager secretsManager) {
    APICoreProperties.DataSource ds = apiCoreProperties.getDataSource();
    String privateHost = secretsManager.getSecretAsPlainText(ds.getPrivateHostCloudFile());
    String userPwd = secretsManager.getSecretAsPlainText(ds.getUserSecretCloudFile());
    
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(String.format("jdbc:postgresql://%s/%s", privateHost, ds.getDbName()));
    config.setUsername(ds.getUserName());
    config.setPassword(userPwd);
    config.setMinimumIdle(ds.getMinIdleConnPool());
    // TODO (optional): This note is to remember that we can customize pgjdbc driver by sending
    //  various options via query string or addDataSourceProperty. see here:
    //  https://github.com/pgjdbc/pgjdbc#connection-properties
    return new HikariDataSource(config);
  }
  
  @Bean
  @Profile("e2e")
  // a different bean method name is required even if profiles are different else context won't
  // load this bean.
  DataSource hikariLocalDataSource(APICoreProperties apiCoreProperties) {
    APICoreProperties.DataSource ds = apiCoreProperties.getDataSource();
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(String.format("jdbc:postgresql://localhost/%s", ds.getDbName()));
    config.setUsername(ds.getUserName());
    config.setMinimumIdle(ds.getMinIdleConnPool());
    return new HikariDataSource(config);
  }
  
  // https://docs.spring.io/spring/docs/current/spring-framework-reference/data-access.html#tx-prog-template-settings
  @Bean
  @Profile({"production", "e2e"})
  TransactionTemplate transactionTemplate(PlatformTransactionManager platformTransactionManager) {
    TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
    // TODO (optional): specify any transaction settings.
    transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
    return transactionTemplate;
  }
  
  @Bean
  @Profile("production")
  VMService productionVMService(APICoreProperties apiCoreProperties,
                                WebClient.Builder webClientBuilder,
                                SecretsManager secretsManager) {
    return new ProductionVMService(webClientBuilder, apiCoreProperties, secretsManager);
  }
  
  @Bean
  @Profile("e2e")
  VMService localVMService(APICoreProperties apiCoreProperties) {
    return new LocalVMService(apiCoreProperties);
  }
  
  @Bean
  @Profile("production")
  RunnerService productionRunnerService(APICoreProperties apiCoreProperties,
                                        WebClient.Builder webClientBuilder,
                                        SecretsManager secretsManager) {
    APICoreProperties.Services services = apiCoreProperties.getServices();
    String secret = secretsManager.getSecretAsPlainText(services.getBtbrAuthSecretCloudFile());
    String btbrUserAuthHeader = Base64.getEncoder().encodeToString((services.getBtbrAuthUser()
        + ":" + secret).getBytes());
    HttpClient httpClient = HttpClient.create()
        .responseTimeout(Duration.ofMinutes(300));
    WebClient webClient = webClientBuilder
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .defaultHeader("Authorization", btbrUserAuthHeader)
        .build();
    return new ProductionRunnerService(apiCoreProperties, webClient);
  }
  
  @Bean
  @Profile("e2e")
  RunnerService localRunnerService(APICoreProperties apiCoreProperties,
                                   WebClient.Builder webClientBuilder) {
    String localBtbrAutSecret = System.getenv("LOCAL_BTBR_AUTH_SECRET");
    if (localBtbrAutSecret == null) {
      localBtbrAutSecret = "local";
    }
    WebClient webClient = webClientBuilder
        .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
        .defaultHeader("Authorization", localBtbrAutSecret)
        .build();
    return new LocalRunnerService(apiCoreProperties, webClient);
  }
}
