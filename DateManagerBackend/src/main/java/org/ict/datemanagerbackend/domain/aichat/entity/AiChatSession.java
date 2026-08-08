package org.ict.datemanagerbackend.domain.aichat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ict.datemanagerbackend.domain.couple.entity.Couple;
import org.ict.datemanagerbackend.domain.user.entity.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_chat_sessions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AiChatSession {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 채팅 세션 ID (PK)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 유저 (users.id 참조)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "couple_id")
  private Couple couple; // 커플 (couples.id 참조)

  @Column(name = "title", length = 100)
  private String title; // 세션 타이틀

  @Column(name = "session_type", nullable = false, insertable = false, updatable = false)
  private String sessionType; // 세션 구분 (PLANNER - DB 기본값 활용)

  @Column(name = "is_private", nullable = false, insertable = false, updatable = false)
  private Integer isPrivate; // 비밀 비서 플래그 (0/1 - DB 기본값 활용)

  @Column(name = "is_active", nullable = false, insertable = false, updatable = false)
  private Integer isActive; // 활성화 여부 (0/1 - DB 기본값 활용)

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt; // 생성 일시

  @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime updatedAt; // 수정 일시

}
