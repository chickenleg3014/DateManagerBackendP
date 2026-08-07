package org.ict.datemanagerbackend.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity_logs")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserActivityLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 행동 로그 ID (PK)

  @Column(name = "user_id", nullable = false)
  private Long userId; // 유저 ID (1:N 관계, users.id 참조)

  @Column(name = "activity_type", nullable = false)
  private String activityType; // 로그 유형 (AI_FEEDBACK 등)

  @Column(name = "target_matrix", nullable = false)
  private String targetMatrix; // 보정 대상 성향 축

  @Column(name = "score_impact", nullable = false)
  private Integer scoreImpact; // 성향치 변화량 (+/-)

  @Column(name = "created_at", insertable = false, updatable = false)
  private LocalDateTime createdAt; // 생성 일시

}
