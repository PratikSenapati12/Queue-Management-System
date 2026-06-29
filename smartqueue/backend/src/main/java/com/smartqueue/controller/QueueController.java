package com.smartqueue.controller;

import com.smartqueue.dto.TokenRequest;
import com.smartqueue.dto.TokenResponse;
import com.smartqueue.dto.QueueStatusResponse;
import com.smartqueue.service.QueueService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/queue")
@CrossOrigin(origins = "*")   // restrict in production
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    /**
     * POST /api/v1/queue/book
     * Book a new token. Returns token number + estimated wait time.
     */
    @PostMapping("/book")
    public ResponseEntity<TokenResponse> bookToken(@Valid @RequestBody TokenRequest request) {
        TokenResponse response = queueService.bookToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/queue/status
     * Returns full live queue + counter status. Polled every 5s by frontend
     * (WebSocket provides push updates for connected clients).
     */
    @GetMapping("/status")
    public ResponseEntity<QueueStatusResponse> getStatus() {
        return ResponseEntity.ok(queueService.getQueueStatus());
    }

    /**
     * POST /api/v1/queue/call-next/{counterNumber}
     * Admin: call the next token to a specific counter.
     */
    @PostMapping("/call-next/{counterNumber}")
    public ResponseEntity<TokenResponse> callNext(@PathVariable int counterNumber) {
        return ResponseEntity.ok(queueService.callNext(counterNumber));
    }

    /**
     * POST /api/v1/queue/serve/{tokenNumber}
     * Admin: mark a token as fully served.
     */
    @PostMapping("/serve/{tokenNumber}")
    public ResponseEntity<Map<String, String>> serveToken(@PathVariable String tokenNumber) {
        queueService.serveToken(tokenNumber);
        return ResponseEntity.ok(Map.of("message", "Token " + tokenNumber + " marked as served"));
    }

    /**
     * POST /api/v1/queue/skip/{tokenNumber}
     * Admin: skip a token (no-show / skipped).
     */
    @PostMapping("/skip/{tokenNumber}")
    public ResponseEntity<Map<String, String>> skipToken(@PathVariable String tokenNumber) {
        queueService.skipToken(tokenNumber);
        return ResponseEntity.ok(Map.of("message", "Token " + tokenNumber + " skipped"));
    }

    /**
     * GET /api/v1/queue/analytics
     * Returns daily analytics: served count, avg wait, hourly volume, service breakdown.
     */
    @GetMapping("/analytics")
    public ResponseEntity<Object> getAnalytics() {
        return ResponseEntity.ok(queueService.getAnalytics());
    }
}
