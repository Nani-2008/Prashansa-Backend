package com.prashansa.dto;

import com.prashansa.entity.AppUser;

public record UserDto(String id, String phone, String name, String role, String location, String emergencyContact, String profilePicUrl) {

    public static UserDto from(AppUser u) {
        return new UserDto(u.getId(), u.getPhone(), u.getName(), u.getRole(), u.getLocation(), u.getEmergencyContact(), u.getProfilePicUrl());
    }
}
