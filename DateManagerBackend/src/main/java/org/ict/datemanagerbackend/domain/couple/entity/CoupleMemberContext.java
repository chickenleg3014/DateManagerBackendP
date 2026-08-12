package org.ict.datemanagerbackend.domain.couple.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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

/**
 * ⚠️ 아직 Repository/Service/Controller가 없는 "엔티티만 먼저 만들어둔" 상태(2026-08-11 기준).
 * {@link CoupleDailyContext}(그날의 공통 조건: 날씨/예산) 하나에 딸린, "각 개인의 그날 컨디션"을
 * 담는 하위 테이블. 커플 2명이므로 하나의 CoupleDailyContext에는 이 행이 최대 2개(각자 1개씩) 생긴다.
 *
 * <p>예: "나는 오늘 체력 30/100이라 격렬한 액티비티는 힘들다" 같은 정보를 저장해두면,
 * 밸런싱 로직이 이걸 참고해서 두 사람 모두에게 무리 없는 코스를 {@link CoupleSyncReport}로 계산해줄
 * 것으로 설계된 구조다.
 */
// battery(체력 갱신)와 note(메모 수정)만 생성 이후 바뀔 수 있어 개별 setter를 열어둔다.
@Entity
@Table(
    name = "couple_member_contexts",
    uniqueConstraints = {
        // 한 사람이 같은 날짜 컨텍스트에 중복으로 두 번 기록을 남기지 못하게 막는다
        @UniqueConstraint(name = "uq_couple_member_contexts", columnNames = {"couple_daily_context_id", "user_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class CoupleMemberContext {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 개인별 컨텍스트 ID (PK)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "couple_daily_context_id", nullable = false)
  private CoupleDailyContext coupleDailyContext; // 이 기록이 속한 "그날의 커플 컨텍스트" (couple_daily_contexts.id 참조)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 이 컨디션을 입력한 본인 (users.id 참조)

  @Setter
  @Column(name = "battery")
  private Integer battery; // 당일 체력 게이지 (0~100) - 값이 낮을수록 활동적인 코스를 덜 추천하는 데 쓰일 것으로 추정

  // @Lob: Large OBject. Oracle의 CLOB(긴 텍스트) 컬럼과 매핑된다는 뜻으로,
  // VARCHAR2처럼 길이 제한(4000자 등)이 없어 자유 서술형 메모를 담기에 적합하다.
  @Setter
  @Lob
  @Column(name = "note")
  private String note; // 특이사항 메모 (예: "오늘 컨디션 안 좋음", "이 근처는 가봤음" 등 자유 텍스트)

}
