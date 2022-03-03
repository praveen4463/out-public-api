package com.zylitics.api.model;

import com.sendgrid.helpers.mail.objects.Email;

import java.util.List;

public class EmailInfo {
  
  private String from;
  
  private String fromName;
  
  private List<Email> tos;
  
  public String getFrom() {
    return from;
  }
  
  public EmailInfo setFrom(String from) {
    this.from = from;
    return this;
  }
  
  public String getFromName() {
    return fromName;
  }
  
  public EmailInfo setFromName(String fromName) {
    this.fromName = fromName;
    return this;
  }
  
  public List<Email> getTos() {
    return tos;
  }
  
  public EmailInfo setTos(List<Email> tos) {
    this.tos = tos;
    return this;
  }
  
  @Override
  public String toString() {
    return "EmailInfo{" +
        "from='" + from + '\'' +
        ", fromName='" + fromName + '\'' +
        ", tos=" + tos +
        '}';
  }
}
