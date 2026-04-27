package com.prashansa.dto;

public record CreateComplaintRequest(
        String type,
        String description,
        String reporterPhone,
        String reporterRole,
        String location,
        String incidentDate,
        String incidentTime,
        String videoUrl,
        String audioUrl) {
}
