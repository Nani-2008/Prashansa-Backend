package com.prashansa.dto;

public record RegisterRequest(String phone, String password, String name, String location, String emergencyContact) {
}
