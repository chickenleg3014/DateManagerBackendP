package org.ict.datemanagerbackend.domain.aichat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ict.datemanagerbackend.domain.user.entity.User;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id", nullable = false)
  private AiChatSession session; // 세션 (ai_chat_sessions.id 참조)

  @Column(name = "sender_type", nullable = false, length = 20)
  private String senderType; // 발신 주체 (USER, AI)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id")
  private User sender; // 발신 유저 (users.id 참조, AI 발신이면 null)

  @Lob
  @Column(name = "message_text", nullable = false)
  private String messageText; // 메시지 내용 (CLOB 매핑)

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt; // 생성 일시

}
