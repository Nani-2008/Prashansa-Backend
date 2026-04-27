package com.prashansa.session;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class SessionRegistry {

    private final ConcurrentHashMap<String, String> tokenToUserId = new ConcurrentHashMap<>();

    public String createSession(String userId) {
        String token = UUID.randomUUID().toString();
        tokenToUserId.put(token, userId);
        return token;
    }

    public Optional<String> findUserId(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(tokenToUserId.get(token));
    }

    public void invalidate(String token) {
        if (token != null) {
            tokenToUserId.remove(token);
        }
    }
}
