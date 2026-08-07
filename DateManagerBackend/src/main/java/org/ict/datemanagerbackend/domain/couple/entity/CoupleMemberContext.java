package org.ict.datemanagerbackend.domain.couple.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "couple_member_contexts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_couple_member_contexts", columnNames = {"couple_daily_context_id", "user_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CoupleMemberContext {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 개인별 컨텍스트 ID (PK)

  @Column(name = "couple_daily_context_id", nullable = false)
  private Long coupleDailyContextId; // 일자별 컨텍스트 ID (couple_daily_contexts.id 참조)

  @Column(name = "user_id", nullable = false)
  private Long userId; // 유저 ID (users.id 참조)

  @Column(name = "battery")
  private Integer battery; // 당일 체력 게이지 (0~100)

  @Lob
  @Column(name = "note")
  private String note; // 특이사항 메모 (CLOB 매핑)

}