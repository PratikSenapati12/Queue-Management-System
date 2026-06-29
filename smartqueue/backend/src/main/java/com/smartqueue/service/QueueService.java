package com.smartqueue.service;

import com.smartqueue.dto.TokenRequest;
import com.smartqueue.dto.TokenResponse;
import com.smartqueue.dto.QueueStatusResponse;
import com.smartqueue.model.Counter;
import com.smartqueue.model.Token;
import com.smartqueue.model.Token.Priority;
import com.smartqueue.model.Token.TokenStatus;
import com.smartqueue.repository.CounterRepository;
import com.smartqueue.repository.TokenRepository;
import com.smartqueue.websocket.QueueWebSocketHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class QueueService {

    private static final int AVG_SERVICE_TIME_MIN = 8;  // baseline per token
    private static final String TOKEN_PREFIX      = "A";

    // Thread-safe daily counter (resets at midnight via scheduler)
    private final AtomicInteger dailyCounter = new AtomicInteger(0);

    private final TokenRepository   tokenRepo;
    private final CounterRepository counterRepo;
    private final QueueWebSocketHandler wsHandler;

    public QueueService(TokenRepository tokenRepo, CounterRepository counterRepo,
                        QueueWebSocketHandler wsHandler) {
        this.tokenRepo   = tokenRepo;
        this.counterRepo = counterRepo;
        this.wsHandler   = wsHandler;
    }

    // ── BOOK TOKEN ──────────────────────────────────────────────────────────────
    @Transactional
    public TokenResponse bookToken(TokenRequest request) {
        int seq = dailyCounter.incrementAndGet();
        String tokenNumber = String.format("%s-%03d", TOKEN_PREFIX, seq);

        // Calculate estimated wait: position in queue × avg service time
        long queueSize  = tokenRepo.countWaiting();
        int  activeCtrs = counterRepo.findByActiveTrue().size();
        int  waitMins   = activeCtrs > 0
                ? (int) Math.ceil((double) queueSize / activeCtrs) * AVG_SERVICE_TIME_MIN
                : (int) queueSize * AVG_SERVICE_TIME_MIN;

        // Urgent tokens jump the queue — shorter wait
        if (request.getPriority() == Priority.URGENT)  waitMins = Math.max(0, waitMins - 10);
        if (request.getPriority() == Priority.SENIOR)  waitMins = Math.max(0, waitMins - 5);

        Token token = new Token(tokenNumber, request.getCustomerName(),
                                request.getMobileNumber(), request.getServiceType(),
                                request.getPriority());
        token.setEstimatedWaitMin(waitMins);
        tokenRepo.save(token);

        // Broadcast live update to all connected dashboards
        wsHandler.broadcastQueueUpdate(buildQueueStatus());

        return new TokenResponse(tokenNumber, waitMins, queueSize + 1);
    }

    // ── CALL NEXT TOKEN ─────────────────────────────────────────────────────────
    @Transactional
    public TokenResponse callNext(int counterNumber) {
        Counter counter = counterRepo.findById(counterNumber)
                .orElseThrow(() -> new RuntimeException("Counter not found"));

        if (!counter.isActive())
            throw new RuntimeException("Counter " + counterNumber + " is not active");

        List<Token> queue = tokenRepo.findQueueOrdered();
        if (queue.isEmpty())
            throw new RuntimeException("Queue is empty");

        Token next = queue.get(0);
        next.setStatus(TokenStatus.SERVING);
        next.setCounterNumber(counterNumber);
        next.setCalledAt(LocalDateTime.now());
        tokenRepo.save(next);

        counter.setCurrentToken(next.getTokenNumber());
        counterRepo.save(counter);

        wsHandler.broadcastQueueUpdate(buildQueueStatus());

        return new TokenResponse(next.getTokenNumber(), 0, (long) queue.size() - 1);
    }

    // ── SERVE (CLOSE) TOKEN ─────────────────────────────────────────────────────
    @Transactional
    public void serveToken(String tokenNumber) {
        Token token = tokenRepo.findByTokenNumber(tokenNumber)
                .orElseThrow(() -> new RuntimeException("Token not found: " + tokenNumber));

        token.setStatus(TokenStatus.SERVED);
        token.setServedAt(LocalDateTime.now());

        if (token.getCalledAt() != null) {
            int actual = (int) ChronoUnit.MINUTES.between(token.getCreatedAt(), token.getServedAt());
            token.setActualWaitMin(actual);
        }

        // Free up counter
        if (token.getCounterNumber() != null) {
            counterRepo.findById(token.getCounterNumber()).ifPresent(c -> {
                c.setCurrentToken(null);
                c.setTokensServedToday(c.getTokensServedToday() + 1);
                counterRepo.save(c);
            });
        }

        tokenRepo.save(token);
        wsHandler.broadcastQueueUpdate(buildQueueStatus());
    }

    // ── SKIP TOKEN ───────────────────────────────────────────────────────────────
    @Transactional
    public void skipToken(String tokenNumber) {
        Token token = tokenRepo.findByTokenNumber(tokenNumber)
                .orElseThrow(() -> new RuntimeException("Token not found: " + tokenNumber));
        token.setStatus(TokenStatus.SKIPPED);
        tokenRepo.save(token);
        wsHandler.broadcastQueueUpdate(buildQueueStatus());
    }

    // ── GET QUEUE STATUS ─────────────────────────────────────────────────────────
    public QueueStatusResponse getQueueStatus() {
        return buildQueueStatus();
    }

    // ── ANALYTICS ────────────────────────────────────────────────────────────────
    public Object getAnalytics() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long served    = tokenRepo.countServedToday(startOfDay);
        long waiting   = tokenRepo.countWaiting();
        Double avgWait = tokenRepo.avgWaitTimeToday(startOfDay);
        return new java.util.HashMap<>() {{
            put("servedToday",   served);
            put("waiting",       waiting);
            put("avgWaitMin",    avgWait != null ? Math.round(avgWait * 10) / 10.0 : 0);
            put("hourlyVolume",  tokenRepo.hourlyVolume(startOfDay));
            put("serviceBreakdown", tokenRepo.serviceDistribution(startOfDay));
        }};
    }

    // ── MIDNIGHT RESET ───────────────────────────────────────────────────────────
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyCounter() {
        dailyCounter.set(0);
        // Reset counter stats
        counterRepo.findAll().forEach(c -> {
            c.setTokensServedToday(0);
            counterRepo.save(c);
        });
    }

    // ── INTERNAL HELPERS ────────────────────────────────────────────────────────
    private QueueStatusResponse buildQueueStatus() {
        List<Token>   queue    = tokenRepo.findQueueOrdered();
        List<Counter> counters = counterRepo.findAll();

        List<TokenResponse> queueDTOs = queue.stream()
            .map(t -> new TokenResponse(t.getTokenNumber(),
                                        t.getEstimatedWaitMin(),
                                        (long) queue.indexOf(t) + 1))
            .collect(Collectors.toList());

        return new QueueStatusResponse(queueDTOs, counters, queue.size());
    }
}
