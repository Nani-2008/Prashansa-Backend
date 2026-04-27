package com.prashansa.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prashansa.dto.ComplaintDto;
import com.prashansa.dto.ComplaintPatchRequest;
import com.prashansa.dto.CreateComplaintRequest;
import com.prashansa.entity.AppUser;
import com.prashansa.entity.Complaint;
import com.prashansa.repository.ComplaintRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ComplaintService(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    public List<ComplaintDto> listForUser(AppUser user) {
        List<Complaint> all = complaintRepository.findAll();
        return all.stream().filter(c -> visibleTo(c, user)).map(ComplaintDto::from).toList();
    }

    private boolean visibleTo(Complaint c, AppUser user) {
        String role = user.getRole();
        if ("admin".equals(role)) {
            return true;
        }
        if ("police".equals(role)) {
            return "police".equals(c.getAssignedTo());
        }
        if ("lawyer".equals(role)) {
            return "lawyer".equals(c.getAssignedTo());
        }
        if ("counsellor".equals(role)) {
            return "counsellor".equals(c.getAssignedTo());
        }
        return Objects.equals(c.getReporterPhone(), user.getPhone());
    }

    @Transactional
    public ComplaintDto create(AppUser currentUser, CreateComplaintRequest req) {
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        String id = generateUniqueCaseId();
        String reporterPhone = req.reporterPhone() != null && !req.reporterPhone().isBlank()
                ? normalizePhone(req.reporterPhone())
                : currentUser.getPhone();
        String date = java.time.LocalDate.now().toString();
        List<Map<String, Object>> history = new ArrayList<>();
        String by = "victim".equals(req.reporterRole()) ? "Victim" : "Witness";
        Map<String, Object> createdEntry = new java.util.LinkedHashMap<>();
        createdEntry.put("action", "Created");
        createdEntry.put("by", by);
        createdEntry.put("to", null);
        createdEntry.put("timestamp", Instant.now().toString());
        history.add(createdEntry);
        Complaint c = new Complaint();
        c.setId(id);
        c.setDate(date);
        c.setType(req.type());
        c.setStatus("pending");
        c.setDescription(req.description());
        c.setAssignedTo(null);
        c.setReporterPhone(reporterPhone);
        c.setReporterRole(req.reporterRole() != null ? req.reporterRole() : "victim");
        c.setLocation(req.location());
        c.setIncidentDate(req.incidentDate());
        c.setIncidentTime(req.incidentTime());
        c.setVideoUrl(req.videoUrl());
        c.setAudioUrl(req.audioUrl());
        c.setAssignmentHistoryJson(writeHistory(history));
        complaintRepository.save(c);
        return ComplaintDto.from(c);
    }

    private String generateCaseId() {
        int year = java.time.Year.now().getValue();
        String rnd = String.format("%06d", (int) (Math.random() * 1_000_000));
        return "CASE-" + year + "-" + rnd;
    }

    private String generateUniqueCaseId() {
        for (int i = 0; i < 10; i++) {
            String candidate = generateCaseId();
            if (!complaintRepository.existsById(candidate)) {
                return candidate;
            }
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not allocate unique case ID");
    }

    @Transactional
    public ComplaintDto patch(String complaintId, AppUser user, ComplaintPatchRequest patch) {
        Complaint c = complaintRepository
                .findById(complaintId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found"));
        authorizePatch(user, c, patch);

        // Handle user resolution actions
        if (patch.resolutionStatus() != null) {
            if ("unresolved".equals(patch.resolutionStatus())) {
                c.setAssignedTo(null);
                c.setStatus("pending");
            } else if ("resolved".equals(patch.resolutionStatus())) {
                c.setStatus("resolved");
            }
        }

        if (patch.status() != null && patch.resolutionStatus() == null) {
            c.setStatus(patch.status());
        }
        if (patch.assignedTo() != null && patch.resolutionStatus() == null) {
            c.setAssignedTo(patch.assignedTo());
        }
        if (patch.historyEntry() != null) {
            List<Map<String, Object>> list = readHistory(c.getAssignmentHistoryJson());
            list.add(new java.util.LinkedHashMap<>(patch.historyEntry()));
            c.setAssignmentHistoryJson(writeHistory(list));
        }
        complaintRepository.save(c);
        return ComplaintDto.from(c);
    }

    private void authorizePatch(AppUser user, Complaint c, ComplaintPatchRequest patch) {
        String role = user.getRole();
        if ("admin".equals(role)) {
            if (patch.assignedTo() != null
                    && !"police".equals(patch.assignedTo())
                    && !"lawyer".equals(patch.assignedTo())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Admin can only assign to police or lawyer");
            }
            return;
        }
        // Allow regular users to resolve/unresolve their own complaints
        if ("user".equals(role) || (!"police".equals(role) && !"lawyer".equals(role) && !"counsellor".equals(role) && !"admin".equals(role))) {
            if (patch.resolutionStatus() != null && Objects.equals(c.getReporterPhone(), user.getPhone())) {
                if (c.getAssignedTo() == null && !"resolved".equals(c.getStatus())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Case must be assigned before resolving");
                }
                return;
            }
        }
        if ("police".equals(role)) {
            boolean ok = "pending".equals(c.getStatus()) || "police".equals(c.getAssignedTo());
            if (!ok) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot update this complaint");
            }
            if (patch.assignedTo() != null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Police cannot reassign");
            }
            return;
        }
        if ("lawyer".equals(role)) {
            if (!"lawyer".equals(c.getAssignedTo())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not assigned to you");
            }
            if (patch.status() != null && !patch.status().equals("verified") && !patch.status().equals("rejected")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lawyer can only verify or reject cases");
            }
            if (patch.assignedTo() != null && !"counsellor".equals(patch.assignedTo())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Lawyer can only assign to counsellor");
            }
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
    }

    @Transactional
    public void updateLiveLocation(AppUser user, String location) {
        if (location == null || location.isBlank()) return;
        List<Complaint> userComplaints = complaintRepository.findAll().stream()
                .filter(c -> user.getPhone().equals(c.getReporterPhone()))
                .filter(c -> !"resolved".equals(c.getStatus()))
                .toList();
        
        for (Complaint c : userComplaints) {
            c.setLocation(location);
            complaintRepository.save(c);
        }
    }

    @Transactional
    public void delete(String id, AppUser user) {
        if (!"admin".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can delete cases");
        }
        Complaint c = complaintRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found"));
        complaintRepository.delete(c);
    }

    private List<Map<String, Object>> readHistory(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return new ArrayList<>(objectMapper.readValue(json, new TypeReference<>() {}));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String writeHistory(List<Map<String, Object>> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot serialize history");
        }
    }

    private static String normalizePhone(String phone) {
        return phone.replaceAll("\\D", "");
    }
}
