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

import java.time.LocalDateTime;

@Entity
@Table(
    name = "couple_sync_reports",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_couple_sync_reports_case", columnNames = {"case_number"})
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CoupleSyncReport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 리포트 ID (PK)

  @Column(name = "couple_id", nullable = false)
  private Long coupleId; // 커플 ID (couples.id 참조)

  @Column(name = "case_number", nullable = false, length = 50)
  private String caseNumber; // 영수증 콘셉트 고유 난수 코드

  @Column(name = "balancing_score")
  private Integer balancingScore; // 밸런싱 반영 점수

  @Column(name = "conflict_probability_text", length = 100)
  private String conflictProbabilityText; // 예상 갈등 요인 및 분석 텍스트

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt; // 생성 일시

}