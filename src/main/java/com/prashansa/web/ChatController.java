package com.prashansa.web;

import com.prashansa.dto.ChatMessageDto;
import com.prashansa.dto.SendChatMessageRequest;
import com.prashansa.entity.AppUser;
import com.prashansa.service.ChatService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{complaintId}/messages")
    public List<ChatMessageDto> listMessages(
            @PathVariable String complaintId,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTR) AppUser currentUser) {
        return chatService.listMessages(complaintId, currentUser);
    }

    @PostMapping("/{complaintId}/messages")
    public ChatMessageDto sendMessage(
            @PathVariable String complaintId,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTR) AppUser currentUser,
            @RequestBody SendChatMessageRequest body) {
        return chatService.sendMessage(complaintId, currentUser, body.content());
    }
}
