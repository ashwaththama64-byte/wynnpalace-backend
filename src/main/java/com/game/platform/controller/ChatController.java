package com.game.platform.controller;

import com.game.platform.entity.ChatMessage;
import com.game.platform.service.ChatService;

import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin("*")
public class ChatController {

    private final ChatService service;
    private final SimpMessagingTemplate template;

    public ChatController(ChatService service, SimpMessagingTemplate template) {
        this.service = service;
        this.template = template;
    }

    // =========================
    // 🔥 WEBSOCKET SEND
    // =========================
    @MessageMapping("/send")
    public void sendMessage(ChatMessage msg) {

        String ticketId = msg.getTicketId();

        boolean isNew = false;

        // 🔥 FIRST MESSAGE → CREATE TICKET
        if (ticketId == null || ticketId.isEmpty()) {
            ticketId = service.generateTicketId();
            isNew = true;
        }

        ChatMessage saved = service.save(
                msg.getSender(),
                msg.getUsername(),
                msg.getUserId(),
                msg.getMessage(),
                msg.getImageUrl(),
                ticketId
        );

        // =========================
        // 🔥 SEND TO USER (INITIAL HANDSHAKE)
        // =========================
        if (isNew) {
            // 👇 VERY IMPORTANT (user listens here first)
            template.convertAndSend("/topic/init/" + msg.getUsername(), saved);
        }

        // =========================
        // 🔥 NORMAL FLOW (TICKET)
        // =========================
        template.convertAndSend("/topic/ticket/" + ticketId, saved);

        // =========================
        // 🔥 ADMIN PANEL
        // =========================
        template.convertAndSend("/topic/admin", saved);
    }
    // =========================
    // 👤 USER CHAT HISTORY
    // =========================
    @GetMapping("/user/{username}")
    public List<ChatMessage> getUserChat(@PathVariable String username) {
        return service.getUserChat(username);
    }

    // =========================
    // 👨‍💼 ADMIN APIs
    // =========================

    // 🔥 OPEN TICKETS (FIFO)
    @GetMapping("/admin/open")
    public List<String> getOpenTickets() {
        return service.getOpenTickets();
    }

    // 🔥 CLOSED TICKETS
    @GetMapping("/admin/closed")
    public List<String> getClosedTickets() {
        return service.getClosedTickets();
    }

    // 🔥 CHAT BY TICKET
    @GetMapping("/admin/chat/{ticketId}")
    public List<ChatMessage> getTicketChat(@PathVariable String ticketId) {
        return service.getTicketChat(ticketId);
    }

    // 🔥 CLOSE TICKET
    @PostMapping("/admin/close/{ticketId}")
    public void closeTicket(@PathVariable String ticketId) {

        service.closeTicket(ticketId);

        ChatMessage msg = new ChatMessage();
        msg.setStatus("CLOSED");
        msg.setTicketId(ticketId);

        // 🔥 notify user also
        template.convertAndSend("/topic/ticket/" + ticketId, msg);

        // 🔥 notify admin
        template.convertAndSend("/topic/admin", msg);
    }
}