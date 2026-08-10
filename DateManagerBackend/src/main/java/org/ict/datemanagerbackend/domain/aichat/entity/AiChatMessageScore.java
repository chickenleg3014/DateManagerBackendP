package org.ict.datemanagerbackend.domain.aichat.entity;

import jakarta.persistence.*;
import lombok.*;

// 메시지 분석 시점에 한 번 계산되는 값이라 생성 이후 바뀌지 않아 setter가 없다.
@Entity
@Table(
    name = "ai_chat_message_scores",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_ai_chat_message_scores", columnNames = {"message_id", "score_type"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class AiChatMessageScore {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 점수 매핑 ID (PK)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "message_id", nullable = false)
  private AiChatMessage message; // 메시지 (ai_chat_messages.id 참조)

  @Column(name = "score_type", nullable = false, length = 30)
  private String scoreType; // 점수 종류 (ENERGY 등)

  @Column(name = "score_value", nullable = false)
  private Integer scoreValue; // 매핑된 점수 값

}
