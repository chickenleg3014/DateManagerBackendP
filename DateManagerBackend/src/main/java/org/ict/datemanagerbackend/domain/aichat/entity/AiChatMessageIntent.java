package org.ict.datemanagerbackend.domain.aichat.entity;

import jakarta.persistence.*;
import lombok.*;

// 메시지 분석 시점에 한 번 태깅되는 값이라 생성 이후 바뀌지 않아 setter가 없다.
@Entity
@Table(
    name = "ai_chat_message_intents",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_ai_chat_message_intents", columnNames = {"message_id", "intent_tag"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class AiChatMessageIntent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 의도 태그 ID (PK)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "message_id", nullable = false)
  private AiChatMessage message; // 메시지 (ai_chat_messages.id 참조)

  @Column(name = "intent_tag", nullable = false, length = 100)
  private String intentTag; // 검색 의도 태그

}
