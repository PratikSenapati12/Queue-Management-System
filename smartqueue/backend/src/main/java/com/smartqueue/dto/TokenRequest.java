package com.smartqueue.dto;

import com.smartqueue.model.Token.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// ── TOKEN REQUEST ────────────────────────────────────────────────────────────
// Used as POST body when booking a token
public class TokenRequest {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String mobileNumber;

    @NotBlank(message = "Service type is required")
    private String serviceType;

    private Priority priority = Priority.NORMAL;

    public String getCustomerName()         { return customerName; }
    public void setCustomerName(String n)   { this.customerName = n; }
    public String getMobileNumber()         { return mobileNumber; }
    public void setMobileNumber(String m)   { this.mobileNumber = m; }
    public String getServiceType()          { return serviceType; }
    public void setServiceType(String s)    { this.serviceType = s; }
    public Priority getPriority()           { return priority; }
    public void setPriority(Priority p)     { this.priority = p; }
}
