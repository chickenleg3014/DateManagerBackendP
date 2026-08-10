package org.ict.datemanagerbackend.domain.couple.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ict.datemanagerbackend.domain.user.entity.User;

import java.time.LocalDateTime;

// roleType(역할 위임)과 leftAt(탈퇴 처리)만 생성 이후 바뀔 수 있어 개별 setter를 열어둔다.
@Entity
@Table(
    name = "couple_members",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_couple_members", columnNames = {"couple_id", "user_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class CoupleMember {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 멤버십 ID (PK)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "couple_id", nullable = false)
  private Couple couple; // 커플 (couples.id 참조)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 유저 (users.id 참조)

  @Setter
  @Column(name = "role_type", length = 20)
  private String roleType; // 권한 타입 (OWNER, PARTNER 등)

  @Column(name = "joined_at", insertable = false, updatable = false)
  private LocalDateTime joinedAt; // 참여 일시

  @Setter
  @Column(name = "left_at")
  private LocalDateTime leftAt; // 이탈 일시

}
