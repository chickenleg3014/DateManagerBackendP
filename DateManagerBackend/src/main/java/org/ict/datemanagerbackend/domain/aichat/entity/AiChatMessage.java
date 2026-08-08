package org.ict.datemanagerbackend.domain.aichat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_chat_messages")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AiChatMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 메시지 ID (PK)

  @Column(name = "session_id", nullable = false)
  private Long sessionId; // 세션 ID (ai_chat_sessions.id 참조)

  @Column(name = "sender_type", nullable = false, length = 20)
  private String senderType; // 발신 주체 (USER, AI)

  @Column(name = "sender_id")
  private Long senderId; // 발신 유저 ID (users.id 참조)

  @Lob
  @Column(name = "message_text", nullable = false)
  private String messageText; // 메시지 내용 (CLOB 매핑)

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt; // 생성 일시

}