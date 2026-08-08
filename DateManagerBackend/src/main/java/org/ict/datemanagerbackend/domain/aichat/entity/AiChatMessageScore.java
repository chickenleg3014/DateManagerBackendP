package org.ict.datemanagerbackend.domain.aichat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "ai_chat_message_scores",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_ai_chat_message_scores", columnNames = {"message_id", "score_type"})
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AiChatMessageScore {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 점수 매핑 ID (PK)

  @Column(name = "message_id", nullable = false)
  private Long messageId; // 메시지 ID (ai_chat_messages.id 참조)

  @Column(name = "score_type", nullable = false, length = 30)
  private String scoreType; // 점수 종류 (ENERGY 등)

  @Column(name = "score_value", nullable = false)
  private Integer scoreValue; // 매핑된 점수 값

}