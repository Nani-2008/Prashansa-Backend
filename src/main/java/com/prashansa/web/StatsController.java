package com.prashansa.web;

import com.prashansa.repository.AppUserRepository;
import com.prashansa.repository.ComplaintRepository;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final ComplaintRepository complaintRepository;
    private final AppUserRepository userRepository;

    public StatsController(ComplaintRepository complaintRepository, AppUserRepository userRepository) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public Map<String, Long> getStats() {
        long complaintsCount = complaintRepository.count();
        long expertsCount = userRepository.countByRoleIn(List.of("lawyer", "counsellor", "police"));
        
        return Map.of(
                "complaints", complaintsCount,
                "experts", expertsCount
        );
    }
}
