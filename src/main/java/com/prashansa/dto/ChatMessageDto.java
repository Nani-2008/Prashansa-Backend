package com.prashansa.dto;

import com.prashansa.entity.ChatMessage;
import java.time.Instant;

public record ChatMessageDto(
        Long id,
        String complaintId,
        String senderUserId,
        String senderRole,
        String senderName,
        String content,
        Instant sentAt) {
    public static ChatMessageDto from(ChatMessage message) {
        return new ChatMessageDto(
                message.getId(),
                message.getComplaintId(),
                message.getSenderUserId(),
                message.getSenderRole(),
                message.getSenderName(),
                message.getContent(),
                message.getSentAt());
    }
}
