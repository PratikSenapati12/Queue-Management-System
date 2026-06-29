package com.smartqueue.dto;

public class TokenResponse {

    private String tokenNumber;
    private int    estimatedWaitMin;
    private long   positionInQueue;
    private String message;

    public TokenResponse() {}

    public TokenResponse(String tokenNumber, int estimatedWaitMin, long positionInQueue) {
        this.tokenNumber      = tokenNumber;
        this.estimatedWaitMin = estimatedWaitMin;
        this.positionInQueue  = positionInQueue;
        this.message          = String.format("Token %s issued. Estimated wait: %d min.",
                                              tokenNumber, estimatedWaitMin);
    }

    public String getTokenNumber()          { return tokenNumber; }
    public int    getEstimatedWaitMin()     { return estimatedWaitMin; }
    public long   getPositionInQueue()      { return positionInQueue; }
    public String getMessage()              { return message; }
}
