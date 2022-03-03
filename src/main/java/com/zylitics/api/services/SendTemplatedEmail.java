package com.zylitics.api.services;

import com.zylitics.api.model.EmailInfo;

import javax.annotation.Nullable;
import java.util.Map;

public class SendTemplatedEmail {
  
  private final EmailInfo emailInfo;
  
  private final String templateId;
  
  private final Map<String, Object> templateData;
  
  public SendTemplatedEmail(EmailInfo emailInfo,
                            String templateId,
                            Map<String, Object> templateData) {
    this.emailInfo = emailInfo;
    this.templateId = templateId;
    this.templateData = templateData;
  }
  
  public EmailInfo getEmailInfo() {
    return emailInfo;
  }
  
  public String getTemplateId() {
    return templateId;
  }
  
  @Nullable
  public Map<String, Object> getTemplateData() {
    return templateData;
  }
  
  @Override
  public String toString() {
    return "SendTemplatedEmail{" +
        "emailInfo=" + emailInfo +
        ", templateId='" + templateId + '\'' +
        ", templateData=" + templateData +
        '}';
  }
}

