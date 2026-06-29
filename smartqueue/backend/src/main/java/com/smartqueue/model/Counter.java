package com.smartqueue.model;

import jakarta.persistence.*;

@Entity
@Table(name = "counters")
public class Counter {

    @Id
    private Integer counterNumber;

    private String operatorName;
    private boolean active;
    private String serviceType;     // which service this counter handles (null = all)
    private String currentToken;    // token currently being served

    private int tokensServedToday;
    private double avgServiceTimeMin;

    // ── Constructors ──
    public Counter() {}

    public Counter(Integer counterNumber, String operatorName, boolean active) {
        this.counterNumber = counterNumber;
        this.operatorName  = operatorName;
        this.active        = active;
    }

    // ── Getters & Setters ──
    public Integer getCounterNumber()              { return counterNumber; }
    public void setCounterNumber(Integer c)        { this.counterNumber = c; }
    public String getOperatorName()                { return operatorName; }
    public void setOperatorName(String o)          { this.operatorName = o; }
    public boolean isActive()                      { return active; }
    public void setActive(boolean a)               { this.active = a; }
    public String getServiceType()                 { return serviceType; }
    public void setServiceType(String s)           { this.serviceType = s; }
    public String getCurrentToken()                { return currentToken; }
    public void setCurrentToken(String t)          { this.currentToken = t; }
    public int getTokensServedToday()              { return tokensServedToday; }
    public void setTokensServedToday(int t)        { this.tokensServedToday = t; }
    public double getAvgServiceTimeMin()           { return avgServiceTimeMin; }
    public void setAvgServiceTimeMin(double a)     { this.avgServiceTimeMin = a; }
}
