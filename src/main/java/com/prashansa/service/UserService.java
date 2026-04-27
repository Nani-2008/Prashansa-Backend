package com.prashansa.service;

import com.prashansa.dto.CreateUserRequest;
import com.prashansa.entity.AppUser;
import com.prashansa.repository.AppUserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final AppUserRepository userRepository;

    public UserService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<AppUser> listAll() {
        return userRepository.findAll();
    }

    @Transactional
    public AppUser createUser(CreateUserRequest req) {
        String phone = normalizePhone(req.phone());
        if (userRepository.findByPhone(phone).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with phone exists");
        }
        String role = req.role() != null ? req.role() : "user";
        AppUser u = new AppUser(
                UUID.randomUUID().toString(),
                phone,
                req.name(),
                role,
                req.location() != null ? req.location() : "",
                "password123");
        return userRepository.save(u);
    }

    @Transactional
    public AppUser updateRole(String userId, String newRole) {
        AppUser u = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        u.setRole(newRole);
        return userRepository.save(u);
    }

    @Transactional
    public void deleteUser(String userId) {
        AppUser u = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        userRepository.delete(u);
    }

    private static String normalizePhone(String phone) {
        if (phone == null) {
            throw new IllegalArgumentException("Phone required");
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new IllegalArgumentException("Phone must be 10 digits");
        }
        return digits;
    }
}
