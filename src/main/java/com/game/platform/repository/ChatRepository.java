package com.game.platform.repository;

import com.game.platform.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {

    // =========================
    // 👤 USER CHAT
    // =========================
    List<ChatMessage> findByUsernameOrderByTimeAsc(String username);

    // =========================
    // 🎫 TICKET CHAT
    // =========================
    List<ChatMessage> findByTicketIdOrderByTimeAsc(String ticketId);

    // =========================
    // 🔥 OPEN TICKETS (FINAL FIX)
    // =========================
    @Query(value = """
        SELECT ticket_id
        FROM chat_message
        WHERE status = 'OPEN'
        GROUP BY ticket_id
        ORDER BY MAX(time) DESC
    """, nativeQuery = true)
    List<String> findOpenTickets();

    // =========================
    // 🔥 CLOSED TICKETS (FINAL FIX)
    // =========================
    @Query(value = """
        SELECT ticket_id
        FROM chat_message
        WHERE status = 'CLOSED'
        GROUP BY ticket_id
        ORDER BY MAX(time) DESC
    """, nativeQuery = true)
    List<String> findClosedTickets();
}