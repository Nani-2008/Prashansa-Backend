package com.prashansa.dto;

public record ChangePasswordRequest(String currentPassword, String newPassword) {
}
