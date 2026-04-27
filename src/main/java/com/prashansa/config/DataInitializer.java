package com.prashansa.config;

import com.prashansa.entity.AppUser;
import com.prashansa.entity.Complaint;
import com.prashansa.repository.AppUserRepository;
import com.prashansa.repository.ComplaintRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final ComplaintRepository complaintRepository;

    public DataInitializer(AppUserRepository userRepository, ComplaintRepository complaintRepository) {
        this.userRepository = userRepository;
        this.complaintRepository = complaintRepository;
    }

    @Override
    public void run(String... args) {
        userRepository.save(new AppUser("admin001", "9999999999", "Admin User", "admin", "Headquarters", "password123"));
        userRepository.save(new AppUser("police001", "8888888888", "Police Officer", "police", "Central Station", "password123"));
        userRepository.save(new AppUser("lawyer001", "7777777777", "Adv. John Doe", "lawyer", "Legal Aid Center", "password123"));
        userRepository.save(new AppUser("counsellor001", "6666666666", "Dr. Jane Smith", "counsellor", "Support Center", "password123"));

        complaintRepository.save(complaint(
                "CASE-2026-001",
                "2026-01-15",
                "Physical Violence",
                "verified",
                "Victim reported severe physical assault by partner. Multiple injuries documented. Immediate intervention required.",
                "police",
                "9876543210",
                "victim",
                "Hyderabad",
                null,
                null,
                null,
                "[{\"action\":\"Created\",\"by\":\"Victim\",\"to\":null,\"timestamp\":\"2026-01-15T10:30:00Z\"},"
                        + "{\"action\":\"Assigned to Police\",\"by\":\"Admin\",\"to\":\"police\",\"timestamp\":\"2026-01-15T14:00:00Z\"},"
                        + "{\"action\":\"Under Review\",\"by\":\"Police\",\"to\":null,\"timestamp\":\"2026-01-15T16:00:00Z\"},"
                        + "{\"action\":\"Verification in Progress\",\"by\":\"Police\",\"to\":null,\"timestamp\":\"2026-01-16T08:00:00Z\"},"
                        + "{\"action\":\"Verified\",\"by\":\"Police\",\"to\":null,\"timestamp\":\"2026-01-16T09:00:00Z\"}]"));

        complaintRepository.save(complaint(
                "CASE-2026-002",
                "2026-01-18",
                "Emotional Abuse",
                "pending",
                "Continuous psychological harassment and threats. Victim experiencing severe mental distress. Support and counseling needed.",
                null,
                "9876543211",
                "witness",
                "Visakhapatnam",
                null,
                null,
                null,
                "[{\"action\":\"Created\",\"by\":\"Witness\",\"to\":null,\"timestamp\":\"2026-01-18T14:20:00Z\"},"
                        + "{\"action\":\"Assigned to Police\",\"by\":\"Admin\",\"to\":\"police\",\"timestamp\":\"2026-01-18T15:00:00Z\"}]"));

        complaintRepository.save(complaint(
                "CASE-2026-003",
                "2026-01-20",
                "Financial Abuse",
                "pending",
                "Suspect controlling finances and preventing access to money. Victim unable to meet basic needs.",
                "counsellor",
                "9876543212",
                "victim",
                "Bangalore",
                null,
                null,
                null,
                "[{\"action\":\"Created\",\"by\":\"Victim\",\"to\":null,\"timestamp\":\"2026-01-20T09:00:00Z\"},"
                        + "{\"action\":\"Assigned to Counsellor\",\"by\":\"Admin\",\"to\":\"counsellor\",\"timestamp\":\"2026-01-20T11:00:00Z\"},"
                        + "{\"action\":\"Under Review\",\"by\":\"Counsellor\",\"to\":null,\"timestamp\":\"2026-01-20T14:00:00Z\"}]"));

        complaintRepository.save(complaint(
                "CASE-2026-004",
                "2026-01-10",
                "Verbal Abuse",
                "rejected",
                "Report filed but investigation found insufficient evidence. Case closed after thorough review.",
                "police",
                "9876543213",
                "victim",
                "Chennai",
                null,
                null,
                null,
                "[{\"action\":\"Created\",\"by\":\"Victim\",\"to\":null,\"timestamp\":\"2026-01-10T10:00:00Z\"},"
                        + "{\"action\":\"Assigned to Police\",\"by\":\"Admin\",\"to\":\"police\",\"timestamp\":\"2026-01-10T12:00:00Z\"},"
                        + "{\"action\":\"Under Review\",\"by\":\"Police\",\"to\":null,\"timestamp\":\"2026-01-10T15:00:00Z\"},"
                        + "{\"action\":\"Verification in Progress\",\"by\":\"Police\",\"to\":null,\"timestamp\":\"2026-01-11T09:00:00Z\"},"
                        + "{\"action\":\"Rejected - Insufficient Evidence\",\"by\":\"Police\",\"to\":null,\"timestamp\":\"2026-01-12T11:00:00Z\"}]"));
    }

    private static Complaint complaint(
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
            String historyJson) {
        Complaint c = new Complaint();
        c.setId(id);
        c.setDate(date);
        c.setType(type);
        c.setStatus(status);
        c.setDescription(description);
        c.setAssignedTo(assignedTo);
        c.setReporterPhone(reporterPhone);
        c.setReporterRole(reporterRole);
        c.setLocation(location);
        c.setIncidentDate(incidentDate);
        c.setIncidentTime(incidentTime);
        c.setVideoUrl(videoUrl);
        c.setAssignmentHistoryJson(historyJson);
        return c;
    }
}
