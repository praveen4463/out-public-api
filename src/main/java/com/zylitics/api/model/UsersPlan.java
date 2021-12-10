package com.zylitics.api.model;

public class UsersPlan {
  
  private PlanType planType;
  
  private String planName;
  
  private String displayName;
  
  private int consumedMinutes;
  
  private int totalParallel;
  
  private int totalMinutes;
  
  private long billingCycleStart;
  
  private long billingCyclePlannedEnd;
  
  public PlanType getPlanType() {
    return planType;
  }
  
  public UsersPlan setPlanType(PlanType planType) {
    this.planType = planType;
    return this;
  }
  
  public String getPlanName() {
    return planName;
  }
  
  public UsersPlan setPlanName(String planName) {
    this.planName = planName;
    return this;
  }
  
  public String getDisplayName() {
    return displayName;
  }
  
  public UsersPlan setDisplayName(String displayName) {
    this.displayName = displayName;
    return this;
  }
  
  public int getConsumedMinutes() {
    return consumedMinutes;
  }
  
  public UsersPlan setConsumedMinutes(int consumedMinutes) {
    this.consumedMinutes = consumedMinutes;
    return this;
  }
  
  public int getTotalParallel() {
    return totalParallel;
  }
  
  public UsersPlan setTotalParallel(int totalParallel) {
    this.totalParallel = totalParallel;
    return this;
  }
  
  public int getTotalMinutes() {
    return totalMinutes;
  }
  
  public UsersPlan setTotalMinutes(int totalMinutes) {
    this.totalMinutes = totalMinutes;
    return this;
  }
  
  public long getBillingCycleStart() {
    return billingCycleStart;
  }
  
  public UsersPlan setBillingCycleStart(long billingCycleStart) {
    this.billingCycleStart = billingCycleStart;
    return this;
  }
  
  public long getBillingCyclePlannedEnd() {
    return billingCyclePlannedEnd;
  }
  
  public UsersPlan setBillingCyclePlannedEnd(long billingCyclePlannedEnd) {
    this.billingCyclePlannedEnd = billingCyclePlannedEnd;
    return this;
  }
}
