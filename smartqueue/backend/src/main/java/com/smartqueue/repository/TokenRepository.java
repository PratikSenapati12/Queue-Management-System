package com.smartqueue.repository;

import com.smartqueue.model.Token;
import com.smartqueue.model.Token.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    // All tokens in queue sorted by priority (URGENT first) then creation time
    @Query("SELECT t FROM Token t WHERE t.status = 'WAITING' " +
           "ORDER BY CASE t.priority WHEN 'URGENT' THEN 0 WHEN 'SENIOR' THEN 1 ELSE 2 END, t.createdAt ASC")
    List<Token> findQueueOrdered();

    // Currently serving tokens
    List<Token> findByStatus(TokenStatus status);

    // Check duplicate token number
    Optional<Token> findByTokenNumber(String tokenNumber);

    // Count waiting tokens for wait time estimation
    @Query("SELECT COUNT(t) FROM Token t WHERE t.status = 'WAITING'")
    long countWaiting();

    // Tokens served today (for analytics)
    @Query("SELECT COUNT(t) FROM Token t WHERE t.status = 'SERVED' AND t.createdAt >= :startOfDay")
    long countServedToday(LocalDateTime startOfDay);

    // Average actual wait time today
    @Query("SELECT AVG(t.actualWaitMin) FROM Token t WHERE t.status = 'SERVED' AND t.createdAt >= :startOfDay")
    Double avgWaitTimeToday(LocalDateTime startOfDay);

    // Hourly volume for analytics chart
    @Query("SELECT HOUR(t.createdAt), COUNT(t) FROM Token t WHERE t.createdAt >= :startOfDay GROUP BY HOUR(t.createdAt)")
    List<Object[]> hourlyVolume(LocalDateTime startOfDay);

    // Service type distribution
    @Query("SELECT t.serviceType, COUNT(t) FROM Token t WHERE t.createdAt >= :startOfDay GROUP BY t.serviceType")
    List<Object[]> serviceDistribution(LocalDateTime startOfDay);
}
