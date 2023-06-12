package com.zylitics.api.handlers;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.sendgrid.helpers.mail.objects.Email;
import com.zylitics.api.config.APICoreProperties;
import com.zylitics.api.model.*;
import com.zylitics.api.provider.EmailPreferenceProvider;
import com.zylitics.api.services.EmailService;
import com.zylitics.api.services.SendTemplatedEmail;
import com.zylitics.api.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ParallelBuildCompletionEmailHandler {
  
  private static final Logger LOG =
      LoggerFactory.getLogger(ParallelBuildCompletionEmailHandler.class);
  
  private final APICoreProperties apiCoreProperties;
  
  private final EmailService emailService;
  
  private final EmailPreferenceProvider emailPreferenceProvider;
  
  ParallelBuildCompletionEmailHandler(APICoreProperties apiCoreProperties,
                                      EmailService emailService,
                                      EmailPreferenceProvider emailPreferenceProvider) {
    this.apiCoreProperties = apiCoreProperties;
    this.emailService = emailService;
    this.emailPreferenceProvider = emailPreferenceProvider;
  }
  
  private String getLinkToBuild(Build build) {
    APICoreProperties.Email emailProps = apiCoreProperties.getEmail();
    
    String linkToBuild = String.format("%s/%s?project=%s&simple_view=1",
        apiCoreProperties.getFrontEndBaseUrl() + emailProps.getBuildsPage(),
        build.getBuildId(),
        build.getProjectId());
    return String.format("<a href=\"%s\" target=\"_blank\">%s</a>",
        linkToBuild,
        build.getName());
  }
  
  private String utcLocalDateToEmailDate(LocalDateTime utcDate) {
    return ZonedDateTime.of(utcDate, CommonUtil.getUTCZoneId())
        .withZoneSameInstant(ZoneId.of("America/Montreal"))
        .format(DateTimeFormatter.ofPattern("MMM d, h:mm:ss a")) + " EST";
  }
  
  public void handle(BuildRunConfig config,
                     List<Build> builds,
                     boolean allSucceeded,
                     List<TestDetail> testDetails,
                     LocalDateTime start) {
    APICoreProperties.Email emailProps = apiCoreProperties.getEmail();
    
    Build firstBuild = builds.get(0);
    int orgId = firstBuild.getOrganizationId();
    List<String> emails = allSucceeded
        ? emailPreferenceProvider.getEmailsForBuildSuccess(orgId)
        : emailPreferenceProvider.getEmailsForBuildFailure(orgId);
    
    if (emails.size() == 0) {
      return;
    }
    
    String templateId = allSucceeded
        ? emailProps.getEmailParallelBuildSuccessTmpId()
        : emailProps.getEmailParallelBuildFailedTmpId();
    
    List<Email> tos = new ArrayList<>();
    emails.forEach(e -> tos.add(new Email(e)));
    
    EmailInfo emailInfo = new EmailInfo()
        .setFromName(emailProps.getEmailSenderName())
        .setFrom(emailProps.getSupportEmail())
        .setTos(tos);
    
    // Build template data
    
    String buildIdentifier;
    if (!Strings.isNullOrEmpty(config.getBuildName())) {
      buildIdentifier = config.getBuildName();
    } else {
      buildIdentifier = builds.stream().map(b -> String.valueOf(b.getBuildId()))
          .collect(Collectors.joining(" "));
    }
    
    StringBuilder parallelBuildParts = new StringBuilder();
    
    for (Build build : builds) {
      parallelBuildParts.append(
          String.format("<p class=\"build-part\">" +
                  "%s" +
                  (!allSucceeded ? "<span class=\"%s\">%s</span>" : "") + // No need to show status with builds when all succeeded
                  "</p>",
              getLinkToBuild(build),
              build.getFinalStatus() == TestStatus.SUCCESS ? "success" : "failure",
              build.getFinalStatus() == TestStatus.SUCCESS ? "passed" : "failed")
      );
    }
  
    String linkToEmailSettings = String.format("%s?project=%s",
        apiCoreProperties.getFrontEndBaseUrl() + emailProps.getEmailPrefPage(),
        firstBuild.getProjectId());
    
    int totalPassed = 0;
    int totalFailed = 0;
    StringBuilder error = new StringBuilder();
    
    for (TestDetail testDetail : testDetails) {
      if (testDetail.getStatus() == TestStatus.ERROR) {
        // !!For timestamp, we'll convert it to EST for now for all emails.
        // TODO: later put a pref record in db for timezone and convert to that one.
        String failedAt = utcLocalDateToEmailDate(testDetail.getEndDate());
        error.append(
            String.format("<p class=\"test-name\">%s > %s</p>" +
                    "<p class=\"error-detail\">Failed at: %s</p>" +
                    "<p class=\"error-detail\">Build part: %s</p>" +
                    "<p class=\"error-detail\">URL: %s</p>" +
                    "<pre><div class=\"error\">%s</div></pre>",
                testDetail.getFile(),
                testDetail.getTest(),
                failedAt,
                getLinkToBuild(builds.stream()
                    .filter(b -> b.getBuildId() == testDetail.getBuildId())
                    .findFirst().orElseThrow(RuntimeException::new)),
                String.format("<a href=\"%s\" target=\"_blank\">%s</a>",
                    testDetail.getUrlUponError(),
                    testDetail.getUrlUponError()),
                testDetail.getError())
        );
        totalFailed++;
      } else if (testDetail.getStatus() == TestStatus.SUCCESS) {
        totalPassed++;
      }
    }
    
    ImmutableMap.Builder<String, Object> templateDataBuilder = ImmutableMap.builder();
    templateDataBuilder.put("build_identifier", buildIdentifier);
    templateDataBuilder.put("build_parts", parallelBuildParts);
    templateDataBuilder.put("link_to_emails_settings_def_proj", linkToEmailSettings);
    templateDataBuilder.put("start_date", utcLocalDateToEmailDate(start));
    templateDataBuilder.put("end_date",
        utcLocalDateToEmailDate(LocalDateTime.now(CommonUtil.getUTCZoneId())));
    templateDataBuilder.put("passed", totalPassed);
    templateDataBuilder.put("failed", totalFailed);
    templateDataBuilder.put("error", error);
    
    SendTemplatedEmail sendTemplatedEmail = new SendTemplatedEmail(emailInfo,
        templateId,
        templateDataBuilder.build());
    
    emailService.sendAsync(sendTemplatedEmail, null,
        (v) -> LOG.error("Priority: Couldn't send a parallel build completion email to org: " +
            orgId));
  }
}
