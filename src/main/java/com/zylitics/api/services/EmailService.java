package com.zylitics.api.services;

import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import com.zylitics.api.SecretsManager;
import com.zylitics.api.config.APICoreProperties;
import com.zylitics.api.model.EmailInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Service
public class EmailService {
  
  private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);
  
  private static final int RESPONSE_TIMEOUT_SEC = 30;
  
  private static final String API_BASE_PATH = "api.sendgrid.com/v3";
  
  private static final String MAIL_SEND_ENDPOINT = "/mail/send";
  
  private final WebClient webClient;
  
  // !! I've not used sendGrid's library as SendGrid class wasn't thread safe.
  // TODO: for now no rate limiting is considered. Do that once you see those errors, refer
  //  BaseInterface class in SendGrid java client
  public EmailService(WebClient.Builder webClientBuilder,
                      APICoreProperties apiCoreProperties,
                      SecretsManager secretsManager) {
    APICoreProperties.Services services = apiCoreProperties.getServices();
    String secret =
        secretsManager.getSecretAsPlainText(services.getSendgridApiKeySecretCloudFile());
    String authHeader = "Bearer " + secret;
    HttpClient httpClient = HttpClient.create()
        .responseTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT_SEC));
    this.webClient = webClientBuilder
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .baseUrl("https://" + API_BASE_PATH)
        .defaultHeader("Authorization", authHeader)
        .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
  
  public boolean send(SendTemplatedEmail sendTemplatedEmail) {
    return send(buildEmail(sendTemplatedEmail));
  }
  
  public void sendAsync(SendTemplatedEmail sendTemplatedEmail,
                        @Nullable Consumer<ResponseEntity<Void>> onSuccess,
                        @Nullable Consumer<Void> onFailure) {
    Mail mail = buildEmail(sendTemplatedEmail);
    sendAsync(mail, onSuccess, onFailure);
  }
  
  private Mail buildEmail(SendTemplatedEmail sendTemplatedEmail) {
    Mail mail = new Mail();
    EmailInfo emailInfo = sendTemplatedEmail.getEmailInfo();
    mail.setFrom(new Email(emailInfo.getFrom(), emailInfo.getFromName()));
    mail.setTemplateId(sendTemplatedEmail.getTemplateId());
    Personalization personalization = new Personalization();
    emailInfo.getTos().forEach(personalization::addTo);
    Map<String, Object> templateData = new HashMap<>(); // don't mutate, create new
    if (sendTemplatedEmail.getTemplateData() != null) {
      templateData.putAll(sendTemplatedEmail.getTemplateData());
    }
    // add default template data
    if (templateData.get("year") == null) {
      templateData.put("year", LocalDateTime.now().getYear());
    }
    templateData.forEach(personalization::addDynamicTemplateData);
    mail.addPersonalization(personalization);
    return mail;
  }
  
  private String mailToString(Mail mail) {
    try {
      return mail.build();
    } catch (IOException io) {
      LOG.error("Couldn't build mail", io); // log and then throw so that if the caller was async
      throw new RuntimeException("Couldn't build mail", io);
    }
  }
  
  private boolean send(Mail mail) {
    try {
      ResponseEntity<Void> response = webClient.post()
          .uri(MAIL_SEND_ENDPOINT)
          .bodyValue(mailToString(mail))
          .retrieve().toBodilessEntity().block();
      Objects.requireNonNull(response);
      return isSuccess(response.getStatusCode());
    } catch (Throwable t) {
      LOG.error("Couldn't send mail: " + mailToString(mail), t);
      return false;
    }
  }
  
  private void sendAsync(Mail mail,
                         @Nullable Consumer<ResponseEntity<Void>> onSuccess,
                         @Nullable Consumer<Void> onFailure) {
    Mono<ResponseEntity<Void>> response = webClient.post()
        .uri(MAIL_SEND_ENDPOINT)
        .bodyValue(mailToString(mail))
        .retrieve().toBodilessEntity();
    response.subscribe((t) -> {
      if (onSuccess != null) {
        onSuccess.accept(t);
      }
    }, (t) -> {
      // first accept and then log error so that any error in building email doesn't have effect
      // on callback
      if (onFailure != null) {
        onFailure.accept(null);
      }
      LOG.error("Couldn't send mail: " + mailToString(mail), t);
    });
  }
  
  private boolean isSuccess(HttpStatus status) {
    return status == HttpStatus.OK || status == HttpStatus.CREATED || status == HttpStatus.ACCEPTED;
  }
}
