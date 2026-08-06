package org.ict.datemanagerbackend.couple;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "couple_daily_contexts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_couple_daily_contexts", columnNames = {"couple_id", "target_date"})
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CoupleDailyContext {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 컨텍스트 ID (PK)

  @Column(name = "couple_id", nullable = false)
  private Long coupleId; // 커플 ID (couples.id 참조)

  @Column(name = "target_date", nullable = false)
  private LocalDate targetDate; // 데이트 예정일

  @Column(name = "weather_status", length = 20)
  private String weatherStatus; // 날씨 요약 (맑음, 비 등)

  @Column(name = "precipitation_probability")
  private Integer precipitationProbability; // 강수 확률 (0~100)

  @Column(name = "is_indoor_forced", nullable = false, insertable = false, updatable = false)
  private Integer isIndoorForced; // 실내 루트 자동 강제 트리거 (0/1 - DB 기본값 활용)

  @Column(name = "budget_min_per_person", nullable = false, insertable = false, updatable = false)
  private Integer budgetMinPerPerson; // 최소 예산 (인당 - DB 기본값 활용)

  @Column(name = "budget_max_per_person")
  private Integer budgetMaxPerPerson; // 최대 예산 (인당)

  @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime updatedAt; // 수정 일시

}
