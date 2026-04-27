package com.prashansa.service;

import com.prashansa.dto.ChatMessageDto;
import com.prashansa.entity.AppUser;
import com.prashansa.entity.ChatMessage;
import com.prashansa.entity.Complaint;
import com.prashansa.repository.ChatMessageRepository;
import com.prashansa.repository.ComplaintRepository;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ComplaintRepository complaintRepository;

    public ChatService(ChatMessageRepository chatMessageRepository, ComplaintRepository complaintRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.complaintRepository = complaintRepository;
    }

    public List<ChatMessageDto> listMessages(String complaintId, AppUser currentUser) {
        Complaint complaint = requireAccessibleComplaint(complaintId, currentUser);
        enforceConversationAllowed(complaint);
        return chatMessageRepository.findByComplaintIdOrderBySentAtAsc(complaintId).stream()
                .map(ChatMessageDto::from)
                .toList();
    }

    @Transactional
    public ChatMessageDto sendMessage(String complaintId, AppUser currentUser, String content) {
        Complaint complaint = requireAccessibleComplaint(complaintId, currentUser);
        enforceConversationAllowed(complaint);
        String normalized = content != null ? content.trim() : "";
        if (normalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content cannot be empty");
        }
        if (normalized.length() > 4000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message too long");
        }

        ChatMessage msg = new ChatMessage();
        msg.setComplaintId(complaintId);
        msg.setSenderUserId(currentUser.getId());
        msg.setSenderRole(currentUser.getRole());
        msg.setSenderName(resolveName(currentUser));
        msg.setContent(normalized);
        msg.setSentAt(Instant.now());
        chatMessageRepository.save(msg);
        return ChatMessageDto.from(msg);
    }

    private Complaint requireAccessibleComplaint(String complaintId, AppUser currentUser) {
        // Handle admin-* direct channels (admin-police, admin-lawyer, admin-counsellor)
        if (complaintId.startsWith("admin-")) {
            String targetRole = complaintId.substring("admin-".length());
            String role = currentUser.getRole();
            if ("admin".equals(role) || targetRole.equals(role)) {
                return null; // No complaint needed for direct channels
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed for this channel");
        }

        Complaint complaint = complaintRepository
                .findById(complaintId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found"));

        String role = currentUser.getRole();
        if ("admin".equals(role)) {
            return complaint;
        }
        if ("user".equals(role)) {
            if (!Objects.equals(currentUser.getPhone(), complaint.getReporterPhone())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed for this complaint");
            }
            return complaint;
        }
        if ("lawyer".equals(role)) {
            if (!"lawyer".equals(complaint.getAssignedTo())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Complaint is not assigned to lawyer");
            }
            return complaint;
        }
        if ("counsellor".equals(role)) {
            if (!"counsellor".equals(complaint.getAssignedTo())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Complaint is not assigned to counsellor");
            }
            return complaint;
        }
        if ("police".equals(role)) {
            if (!"police".equals(complaint.getAssignedTo())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Complaint is not assigned to police");
            }
            return complaint;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chat access not allowed for this role");
    }

    private static void enforceConversationAllowed(Complaint complaint) {
        // admin-* direct channels return null complaint — always allowed
        if (complaint == null) return;
        if (!"lawyer".equals(complaint.getAssignedTo()) && !"counsellor".equals(complaint.getAssignedTo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Chat is available only when complaint is assigned to lawyer or counsellor");
        }
    }

    private static String resolveName(AppUser user) {
        if (user.getName() != null && !user.getName().isBlank()) {
            return user.getName().trim();
        }
        return user.getRole() + " - " + user.getPhone();
    }
}
