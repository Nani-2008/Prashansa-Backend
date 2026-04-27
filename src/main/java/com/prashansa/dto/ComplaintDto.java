package com.prashansa.dto;

import com.prashansa.entity.Complaint;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record ComplaintDto(
        String id,
        String date,
        String type,
        String status,
        String description,
        String assignedTo,
        String reporterPhone,
        String reporterRole,
        String location,
        String incidentDate,
        String incidentTime,
        String videoUrl,
        String audioUrl,
        List<Map<String, Object>> assignmentHistory) {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public static ComplaintDto from(Complaint c) {
        List<Map<String, Object>> history = Collections.emptyList();
        if (c.getAssignmentHistoryJson() != null && !c.getAssignmentHistoryJson().isBlank()) {
            try {
                history = MAPPER.readValue(c.getAssignmentHistoryJson(), new TypeReference<>() {
                });
            } catch (Exception ignored) {
                history = Collections.emptyList();
            }
        }
        return new ComplaintDto(
                c.getId(),
                c.getDate(),
                c.getType(),
                c.getStatus(),
                c.getDescription(),
                c.getAssignedTo(),
                c.getReporterPhone(),
                c.getReporterRole(),
                c.getLocation(),
                c.getIncidentDate(),
                c.getIncidentTime(),
                c.getVideoUrl(),
                c.getAudioUrl(),
                history);
    }
}
