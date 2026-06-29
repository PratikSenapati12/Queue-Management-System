package com.smartqueue.config;

import com.smartqueue.websocket.QueueWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSecurity
@EnableWebSocket
public class AppConfig implements WebSocketConfigurer {

    private final QueueWebSocketHandler wsHandler;

    public AppConfig(QueueWebSocketHandler wsHandler) {
        this.wsHandler = wsHandler;
    }

    // ── WEBSOCKET ──────────────────────────────────────────────────────────────
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(wsHandler, "/ws/queue")
                .setAllowedOrigins("*");   // restrict in production
    }

    // ── SECURITY ───────────────────────────────────────────────────────────────
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints: book token, check status
                .requestMatchers("/api/v1/queue/book", "/api/v1/queue/status").permitAll()
                // WebSocket
                .requestMatchers("/ws/**").permitAll()
                // Admin endpoints require auth
                .requestMatchers("/api/v1/queue/**").authenticated()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
