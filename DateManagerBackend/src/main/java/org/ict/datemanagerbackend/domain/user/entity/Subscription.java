package org.ict.datemanagerbackend.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// 예전엔 클래스 전체에 @Setter가 붙어있어서 setStatus()가 컴파일도 되고 호출도 됐지만,
// status 컬럼이 updatable=false라 실제로는 DB에 저장이 안 되고 조용히 무시되는 버그가 있었다
// (Couple.status에서 발견됐던 것과 동일한 패턴). status의 updatable만 풀고, 나머지는
// 실제로 값이 바뀌어야 하는 필드에만 개별 @Setter를 붙이는 방식으로 정리했다.
@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class Subscription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 구독 고유 ID (PK)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 회원 (users.id 참조)

  @Setter
  @Column(name = "plan_code", nullable = false)
  private String planCode; // 구독 플랜 코드 (FREE, PREMIUM_MONTHLY 등) - 업그레이드/다운그레이드 시 변경됨

  @Setter
  @Column(nullable = false, insertable = false)
  private String status; // 구독 상태 (ACTIVE, CANCELED, EXPIRED). 생성 시엔 DB 기본값('ACTIVE') 사용, 이후 변경 가능

  @Column(name = "started_at", insertable = false, updatable = false)
  private LocalDateTime startedAt; // 구독 시작일 - DB가 SYSTIMESTAMP로 채우는 값, 생성 이후 절대 안 바뀜

  @Setter
  @Column(name = "expires_at")
  private LocalDateTime expiresAt; // 구독 만료일 - 갱신/연장 시 변경됨

  @Setter
  @Column(name = "payment_provider")
  private String paymentProvider; // 결제 수단/제공자 (IAP, PG 등) - 결제 수단 변경 시 바뀔 수 있음

  @Column(name = "created_at", insertable = false, updatable = false)
  private LocalDateTime createdAt; // 생성 일시 - DB가 관리하는 값이라 setter를 열지 않음

}
