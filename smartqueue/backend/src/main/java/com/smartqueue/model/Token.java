package com.smartqueue.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String tokenNumber;       // e.g. "A-042"

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String mobileNumber;

    @Column(nullable = false)
    private String serviceType;       // GENERAL, BLOOD_TEST, XRAY, PHARMACY

    @Enumerated(EnumType.STRING)
    private Priority priority;        // NORMAL, SENIOR, URGENT

    @Enumerated(EnumType.STRING)
    private TokenStatus status;       // WAITING, SERVING, SERVED, SKIPPED, NO_SHOW

    private Integer counterNumber;    // null until called
    private Integer estimatedWaitMin;
    private Integer actualWaitMin;

    private LocalDateTime createdAt;
    private LocalDateTime calledAt;
    private LocalDateTime servedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = TokenStatus.WAITING;
    }

    // ── Enums ──
    public enum Priority { NORMAL, SENIOR, URGENT }
    public enum TokenStatus { WAITING, SERVING, SERVED, SKIPPED, NO_SHOW }

    // ── Constructors ──
    public Token() {}

    public Token(String tokenNumber, String customerName, String mobileNumber,
                 String serviceType, Priority priority) {
        this.tokenNumber   = tokenNumber;
        this.customerName  = customerName;
        this.mobileNumber  = mobileNumber;
        this.serviceType   = serviceType;
        this.priority      = priority;
    }

    // ── Getters & Setters ──
    public Long getId()                        { return id; }
    public String getTokenNumber()             { return tokenNumber; }
    public void setTokenNumber(String t)       { this.tokenNumber = t; }
    public String getCustomerName()            { return customerName; }
    public void setCustomerName(String n)      { this.customerName = n; }
    public String getMobileNumber()            { return mobileNumber; }
    public void setMobileNumber(String m)      { this.mobileNumber = m; }
    public String getServiceType()             { return serviceType; }
    public void setServiceType(String s)       { this.serviceType = s; }
    public Priority getPriority()              { return priority; }
    public void setPriority(Priority p)        { this.priority = p; }
    public TokenStatus getStatus()             { return status; }
    public void setStatus(TokenStatus s)       { this.status = s; }
    public Integer getCounterNumber()          { return counterNumber; }
    public void setCounterNumber(Integer c)    { this.counterNumber = c; }
    public Integer getEstimatedWaitMin()       { return estimatedWaitMin; }
    public void setEstimatedWaitMin(Integer e) { this.estimatedWaitMin = e; }
    public Integer getActualWaitMin()          { return actualWaitMin; }
    public void setActualWaitMin(Integer a)    { this.actualWaitMin = a; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
    public LocalDateTime getCalledAt()         { return calledAt; }
    public void setCalledAt(LocalDateTime t)   { this.calledAt = t; }
    public LocalDateTime getServedAt()         { return servedAt; }
    public void setServedAt(LocalDateTime t)   { this.servedAt = t; }
}
