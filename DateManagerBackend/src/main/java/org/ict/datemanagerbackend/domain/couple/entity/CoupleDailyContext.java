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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ⚠️ 아직 Repository/Service/Controller가 없는 "엔티티만 먼저 만들어둔" 상태(2026-08-11 기준).
 * DDL의 "2. 커플 성향 밸런싱 코칭 엔진" 섹션에 해당하며, {@link CoupleMemberContext}·
 * {@link CoupleSyncReport}·{@link CoupleSyncReportItem}과 함께 아래 흐름을 위해 설계되었다:
 * <pre>
 *   (오늘의 날씨/예산 등 공통 조건)          (각자의 컨디션)                 (계산 결과)
 *   CoupleDailyContext  ------------- 1:N ------------- CoupleMemberContext
 *          |
 *          | (이 컨텍스트들을 재료로 밸런싱 로직이 계산해서)
 *          v
 *   CoupleSyncReport --- 1:N --- CoupleSyncReportItem --- (N:1) --- Place
 *   (밸런싱 점수/갈등 예측)      (추천 장소 순위 리스트)
 * </pre>
 * 즉 "오늘 우리 커플이 데이트하기 좋은 상황인지, 어디를 가면 좋을지"를 계산하기 위한 입력값 저장소다.
 * 커플 하나가 "오늘 날짜" 기준으로 하나만 가질 수 있도록 (couple_id, target_date) 유니크 제약이 걸려있다.
 */
// weatherStatus/precipitationProbability(날씨 재동기화)와 budgetMaxPerPerson(예산 조정)만
// 생성 이후 바뀔 수 있어 개별 setter를 열어둔다. isIndoorForced/budgetMinPerPerson은 DB 기본값 전용
// (insertable=false, updatable=false라 자바 코드에서는 아예 손댈 수 없고 DB 기본값만 사용).
@Entity
@Table(
    name = "couple_daily_contexts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_couple_daily_contexts", columnNames = {"couple_id", "target_date"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class CoupleDailyContext {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 컨텍스트 ID (PK)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "couple_id", nullable = false)
  private Couple couple; // 커플 (couples.id 참조)

  @Column(name = "target_date", nullable = false)
  private LocalDate targetDate; // 데이트 예정일 (하루 단위로 컨텍스트를 구분하는 키)

  // 아래 두 필드는 향후 WeatherService(기상청 API 연동)가 채워줄 값으로 설계된 것으로 보인다
  // - 이미 만들어진 WeatherController/WeatherService를 그대로 재사용할 수 있는 지점.
  @Setter
  @Column(name = "weather_status", length = 20)
  private String weatherStatus; // 날씨 요약 (맑음, 비 등)

  @Setter
  @Column(name = "precipitation_probability")
  private Integer precipitationProbability; // 강수 확률 (0~100)

  @Column(name = "is_indoor_forced", nullable = false, insertable = false, updatable = false)
  private Integer isIndoorForced; // 실내 루트 자동 강제 트리거 (0/1). 예: 비 확률이 높으면 밸런싱 로직이 실내 장소만 추천하도록 만들 때 쓸 플래그로 추정

  @Column(name = "budget_min_per_person", nullable = false, insertable = false, updatable = false)
  private Integer budgetMinPerPerson; // 최소 예산 (인당 - DB 기본값 활용)

  @Setter
  @Column(name = "budget_max_per_person")
  private Integer budgetMaxPerPerson; // 최대 예산 (인당) - 사용자가 직접 입력/조정 가능하도록 설계된 듯

  @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime updatedAt; // 수정 일시

}
