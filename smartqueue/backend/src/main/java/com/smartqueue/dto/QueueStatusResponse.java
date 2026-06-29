package com.smartqueue.dto;

import com.smartqueue.model.Counter;
import java.util.List;

public class QueueStatusResponse {

    private List<TokenResponse> queue;
    private List<Counter>       counters;
    private int                 totalWaiting;

    public QueueStatusResponse() {}

    public QueueStatusResponse(List<TokenResponse> queue, List<Counter> counters, int totalWaiting) {
        this.queue        = queue;
        this.counters     = counters;
        this.totalWaiting = totalWaiting;
    }

    public List<TokenResponse> getQueue()        { return queue; }
    public List<Counter>       getCounters()     { return counters; }
    public int                 getTotalWaiting() { return totalWaiting; }
}
