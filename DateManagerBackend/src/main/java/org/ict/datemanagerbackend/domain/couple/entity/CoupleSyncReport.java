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

import java.time.LocalDateTime;

/**
 * ⚠️ 아직 Repository/Service/Controller가 없는 "엔티티만 먼저 만들어둔" 상태(2026-08-11 기준).
 * {@link CoupleDailyContext}(그날의 날씨/예산) + {@link CoupleMemberContext}(각자의 컨디션)를
 * 입력값 삼아 밸런싱 로직이 계산해낸 "결과 리포트"를 저장하는 엔티티.
 * 프론트의 마이페이지 메뉴에 있던 "상세 리포트 / 싱크로율 확인" 개념이 이 데이터를 보여주는
 * 화면일 것으로 추정된다.
 *
 * <p>클래스 주석에 적힌 "영수증 콘셉트"란, 편의점 영수증처럼 "오늘 날짜 + 고유 번호(case_number) +
 * 요약 점수 + 상세 항목 리스트({@link CoupleSyncReportItem})"로 구성된 한 장짜리 결과지를
 * 매번 새로 "발급"하는 방식이라는 의미로 보인다 — 그래서 값이 생성 이후 바뀌지 않는(setter 없는)
 * 스냅샷 구조로 설계되어 있다.
 */
// 영수증 콘셉트의 생성 시점 스냅샷이라 이후 값이 바뀌지 않아 setter가 없다(불변 데이터).
@Entity
@Table(
    name = "couple_sync_reports",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_couple_sync_reports_case", columnNames = {"case_number"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class CoupleSyncReport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 리포트 ID (PK)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "couple_id", nullable = false)
  private Couple couple; // 이 리포트의 대상 커플 (couples.id 참조)

  @Column(name = "case_number", nullable = false, length = 50)
  private String caseNumber; // 영수증 콘셉트의 고유 식별 코드(난수) - 리포트를 외부에 공유/조회할 때 쓸 용도로 추정

  @Column(name = "balancing_score")
  private Integer balancingScore; // 밸런싱 반영 점수 - CoupleMemberContext 두 사람의 컨디션 차이 등을 종합한 결과 지표로 추정

  @Column(name = "conflict_probability_text", length = 100)
  private String conflictProbabilityText; // 예상 갈등 요인 및 분석 텍스트 (예: "체력 차이가 커서 무리한 일정은 피하세요")

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt; // 생성 일시 (DB 기본값)

}
