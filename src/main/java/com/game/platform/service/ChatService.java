package com.game.platform.service;

import com.game.platform.entity.ChatMessage;
import com.game.platform.repository.ChatRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    private final ChatRepository repo;

    public ChatService(ChatRepository repo) {
        this.repo = repo;
    }

    // 🔥 GENERATE TICKET
    public String generateTicketId() {
        return "TKT-" + System.currentTimeMillis();
    }

    // 🔥 SAVE MESSAGE
    public ChatMessage save(String sender, String username, String message, String imageUrl, String ticketId) {

        ChatMessage msg = new ChatMessage();
        msg.setSender(sender);
        msg.setUsername(username);
        msg.setMessage(message);
        msg.setImageUrl(imageUrl); // ✅ NEW
        msg.setTime(LocalDateTime.now());
        msg.setStatus("OPEN");
        msg.setTicketId(ticketId);

        return repo.save(msg);
    }
    // 🔥 USER CHAT
    public List<ChatMessage> getUserChat(String username) {
        return repo.findByUsernameOrderByTimeAsc(username);
    }

    // 🔥 ADMIN CHAT
    public List<ChatMessage> getTicketChat(String ticketId) {
        return repo.findByTicketIdOrderByTimeAsc(ticketId);
    }

    // 🔥 CLOSE TICKET
    public void closeTicket(String ticketId) {
        List<ChatMessage> msgs = repo.findByTicketIdOrderByTimeAsc(ticketId);
        msgs.forEach(m -> m.setStatus("CLOSED"));
        repo.saveAll(msgs);
    }

    public List<String> getOpenTickets() {
        return repo.findOpenTickets();
    }

    public List<String> getClosedTickets() {
        return repo.findClosedTickets();
    }
}