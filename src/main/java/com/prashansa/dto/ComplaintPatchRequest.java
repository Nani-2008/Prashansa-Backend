package com.prashansa.dto;

import java.util.Map;

public record ComplaintPatchRequest(String status, String assignedTo, Map<String, Object> historyEntry, String resolutionStatus) {
}
