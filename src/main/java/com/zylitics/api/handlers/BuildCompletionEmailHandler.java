package com.zylitics.api.handlers;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.sendgrid.helpers.mail.objects.Email;
import com.zylitics.api.config.APICoreProperties;
import com.zylitics.api.model.Build;
import com.zylitics.api.model.EmailInfo;
import com.zylitics.api.provider.EmailPreferenceProvider;
import com.zylitics.api.services.EmailService;
import com.zylitics.api.services.SendTemplatedEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class BuildCompletionEmailHandler {
  
  private static final Logger LOG = LoggerFactory.getLogger(BuildCompletionEmailHandler.class);
  
  private final APICoreProperties apiCoreProperties;
  
  private final EmailService emailService;
  
  private final EmailPreferenceProvider emailPreferenceProvider;
  
  BuildCompletionEmailHandler(APICoreProperties apiCoreProperties,
                              EmailService emailService,
                              EmailPreferenceProvider emailPreferenceProvider) {
    this.apiCoreProperties = apiCoreProperties;
    this.emailService = emailService;
    this.emailPreferenceProvider = emailPreferenceProvider;
  }
  
  void handle(Build build, boolean isSuccess, long totalPassed, long totalFailed) {
    APICoreProperties.Email emailProps = apiCoreProperties.getEmail();
    
    List<String> emails = isSuccess
        ? emailPreferenceProvider.getEmailsForBuildSuccess(build.getOrganizationId())
        : emailPreferenceProvider.getEmailsForBuildFailure(build.getOrganizationId());
    
    if (emails.size() == 0) {
      return;
    }
    
    String templateId = isSuccess
        ? emailProps.getEmailBuildSuccessTmpId()
        : emailProps.getEmailBuildFailedTmpId();
    
    List<Email> tos = new ArrayList<>();
    emails.forEach(e -> tos.add(new Email(e)));
    
    EmailInfo emailInfo = new EmailInfo()
        .setFromName(emailProps.getEmailSenderName())
        .setFrom(emailProps.getSupportEmail())
        .setTos(tos);
    
    // Build template data
    
    String buildIdentifier = "#" + build.getBuildId();
    if (!Strings.isNullOrEmpty(build.getBuildName())) {
      buildIdentifier += " " + build.getBuildName();
    }
    
    String linkToBuild = String.format("%s/%s?project=%s&simple_view=1",
        apiCoreProperties.getFrontEndBaseUrl() + emailProps.getBuildsPage(),
        build.getBuildId(),
        build.getProjectId());
    
    String linkToEmailSettings = String.format("%s?project=%s",
        apiCoreProperties.getFrontEndBaseUrl() + emailProps.getEmailPrefPage(),
        build.getProjectId());
    
    Map<String, Object> templateData = ImmutableMap.of(
        "build_identifier", buildIdentifier,
        "link_to_build", linkToBuild,
        "link_to_emails_settings_def_proj", linkToEmailSettings,
        "passed", totalPassed,
        "failed", totalFailed
    );
    
    SendTemplatedEmail sendTemplatedEmail = new SendTemplatedEmail(emailInfo,
        templateId,
        templateData);
    
    emailService.sendAsync(sendTemplatedEmail, null,
        (v) -> LOG.error("Priority: Couldn't send a build completion email to org: " +
            build.getOrganizationId()));
  }
}
