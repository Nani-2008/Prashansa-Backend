package com.prashansa.web;

import com.prashansa.entity.AppUser;
import com.prashansa.repository.AppUserRepository;
import com.prashansa.session.SessionRegistry;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String CURRENT_USER_ATTR = "currentUser";

    private final SessionRegistry sessionRegistry;
    private final AppUserRepository userRepository;
    private final String cookieName;

    public AuthInterceptor(
            SessionRegistry sessionRegistry,
            AppUserRepository userRepository,
            @Value("${app.session.cookie-name}") String cookieName) {
        this.sessionRegistry = sessionRegistry;
        this.userRepository = userRepository;
        this.cookieName = cookieName;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        Optional<String> token = readCookie(request, cookieName);
        if (token.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        Optional<String> userId = sessionRegistry.findUserId(token.get());
        if (userId.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        Optional<AppUser> user = userRepository.findById(userId.get());
        if (user.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        request.setAttribute(CURRENT_USER_ATTR, user.get());
        return true;
    }

    static Optional<String> readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
