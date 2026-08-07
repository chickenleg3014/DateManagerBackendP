package org.ict.datemanagerbackend.domain.aichat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "ai_chat_message_intents",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_ai_chat_message_intents", columnNames = {"message_id", "intent_tag"})
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AiChatMessageIntent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 의도 태그 ID (PK)

  @Column(name = "message_id", nullable = false)
  private Long messageId; // 메시지 ID (ai_chat_messages.id 참조)

  @Column(name = "intent_tag", nullable = false, length = 100)
  private String intentTag; // 검색 의도 태그

}