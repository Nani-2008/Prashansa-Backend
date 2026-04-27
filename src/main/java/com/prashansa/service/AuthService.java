package com.prashansa.service;

import com.prashansa.dto.ChangePasswordRequest;
import com.prashansa.dto.LoginRequest;
import com.prashansa.dto.RegisterRequest;
import com.prashansa.dto.UpdateProfileRequest;
import com.prashansa.entity.AppUser;
import com.prashansa.repository.AppUserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository userRepository;

    public AuthService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AppUser login(LoginRequest req) {
        String phone = normalizePhone(req.phone());
        Optional<AppUser> existing = userRepository.findByPhone(phone);
        
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Mobile number does not exist");
        }
        
        AppUser user = existing.get();
        if (user.getPassword() == null || !user.getPassword().equals(req.password())) {
            // For backward compatibility with seeded users, if password is null, we can either allow it or block it. 
            // We'll require password matching. Seeded users without passwords will need to be updated.
            throw new IllegalArgumentException("Incorrect password");
        }
        
        // Update location on every login
        if (req.location() != null && !req.location().trim().isEmpty()) {
            user.setLocation(req.location().trim());
            userRepository.save(user);
        }
        
        return user;
    }
    
    @Transactional
    public AppUser register(RegisterRequest req) {
        String phone = normalizePhone(req.phone());
        
        if (req.password() == null || req.password().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (req.name() == null || req.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        
        Optional<AppUser> existing = userRepository.findByPhone(phone);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Mobile number already registered");
        }
        
        AppUser u = new AppUser(
            UUID.randomUUID().toString(), 
            phone, 
            req.name(), 
            "user", 
            req.location() != null ? req.location() : "",
            req.password(),
            req.emergencyContact() != null ? req.emergencyContact() : ""
        );
        return userRepository.save(u);
    }

    @Transactional
    public AppUser updateProfile(AppUser user, UpdateProfileRequest req) {
        if (req.name() != null && !req.name().trim().isEmpty()) {
            user.setName(req.name().trim());
        }
        if (req.phone() != null && !req.phone().trim().isEmpty()) {
            String newPhone = normalizePhone(req.phone());
            // Check if new phone is already taken by another user
            Optional<AppUser> existing = userRepository.findByPhone(newPhone);
            if (existing.isPresent() && !existing.get().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Phone number already in use by another account");
            }
            user.setPhone(newPhone);
        }
        if (req.emergencyContact() != null) {
            user.setEmergencyContact(req.emergencyContact().trim());
        }
        return userRepository.save(user);
    }

    @Transactional
    public AppUser changePassword(AppUser user, ChangePasswordRequest req) {
        if (req.currentPassword() == null || req.currentPassword().isEmpty()) {
            throw new IllegalArgumentException("Current password is required");
        }
        if (req.newPassword() == null || req.newPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("New password is required");
        }
        if (!user.getPassword().equals(req.currentPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPassword(req.newPassword());
        return userRepository.save(user);
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
