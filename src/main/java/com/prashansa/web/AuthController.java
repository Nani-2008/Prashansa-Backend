package com.prashansa.web;

import com.prashansa.dto.ChangePasswordRequest;
import com.prashansa.dto.LoginRequest;
import com.prashansa.dto.RegisterRequest;
import com.prashansa.dto.UpdateProfileRequest;
import com.prashansa.dto.UserDto;
import com.prashansa.entity.AppUser;
import com.prashansa.repository.AppUserRepository;
import com.prashansa.service.AuthService;
import com.prashansa.session.SessionRegistry;
import com.prashansa.storage.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SessionRegistry sessionRegistry;
    private final AppUserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final String cookieName;
    private final int maxAgeSeconds;

    public AuthController(
            AuthService authService,
            SessionRegistry sessionRegistry,
            AppUserRepository userRepository,
            FileStorageService fileStorageService,
            @Value("${app.session.cookie-name}") String cookieName,
            @Value("${app.session.max-age-seconds}") int maxAgeSeconds) {
        this.authService = authService;
        this.sessionRegistry = sessionRegistry;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.cookieName = cookieName;
        this.maxAgeSeconds = maxAgeSeconds;
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(
            @RequestBody LoginRequest body, HttpServletRequest request, HttpServletResponse response) {
        try {
            AppUser user = authService.login(body);
            String token = sessionRegistry.createSession(user.getId());
            attachSessionCookie(response, request, token, maxAgeSeconds);
            return ResponseEntity.ok(UserDto.from(user));
        } catch (IllegalArgumentException e) {
            if ("Mobile number does not exist".equals(e.getMessage())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            } else if ("Incorrect password".equals(e.getMessage())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(
            @RequestBody RegisterRequest body, HttpServletRequest request, HttpServletResponse response) {
        try {
            AppUser user = authService.register(body);
            String token = sessionRegistry.createSession(user.getId());
            attachSessionCookie(response, request, token, maxAgeSeconds);
            return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.from(user));
        } catch (IllegalArgumentException e) {
            if ("Mobile number already registered".equals(e.getMessage())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        AuthInterceptor.readCookie(request, cookieName).ifPresent(sessionRegistry::invalidate);
        attachSessionCookie(response, request, "", 0);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(HttpServletRequest request) {
        return AuthInterceptor.readCookie(request, cookieName)
                .flatMap(sessionRegistry::findUserId)
                .flatMap(userRepository::findById)
                .map(UserDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTR) AppUser user,
            @RequestBody UpdateProfileRequest body) {
        try {
            AppUser updated = authService.updateProfile(user, body);
            return ResponseEntity.ok(UserDto.from(updated));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/password")
    public ResponseEntity<UserDto> changePassword(
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTR) AppUser user,
            @RequestBody ChangePasswordRequest body) {
        try {
            AppUser updated = authService.changePassword(user, body);
            return ResponseEntity.ok(UserDto.from(updated));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/profile-pic")
    public ResponseEntity<UserDto> uploadProfilePic(
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTR) AppUser user,
            @RequestParam("file") MultipartFile file) {
        String url = fileStorageService.storeImage(file);
        user.setProfilePicUrl(url);
        userRepository.save(user);
        return ResponseEntity.ok(UserDto.from(user));
    }

    private void attachSessionCookie(HttpServletResponse response, HttpServletRequest request, String token, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofSeconds(maxAge))
                .sameSite("None")
                .secure(true)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

}
