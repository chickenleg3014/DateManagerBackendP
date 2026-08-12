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

@Entity
@Table(name = "subscriptions")
@Getter
@Setter // 우선 롬복으로 생성해 두셨다가, 추후 컨벤션에 맞춰 개별 수정하기 용이하도록 적용했습니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Subscription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 구독 고유 ID (PK)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 회원 (users.id 참조)

  @Column(name = "plan_code", nullable = false)
  private String planCode; // 구독 플랜 코드 (FREE, PREMIUM_MONTHLY 등)

  // 주의: subscriptions 테이블도 ddl-auto로만 생성돼 DB 레벨 DEFAULT가 없다(User.createdAt과 동일한 이유).
  // insertable=false로 DB 기본값에 의존했던 건 실제로는 항상 null/에러가 나는 버그였어서 @Builder.Default로 교체했다.
  @Builder.Default
  @Column(nullable = false)
  private String status = "ACTIVE"; // 구독 상태 (ACTIVE, CANCELED, EXPIRED)

  @Builder.Default
  @Column(name = "started_at", nullable = false)
  private LocalDateTime startedAt = LocalDateTime.now(); // 구독 시작일

  @Column(name = "expires_at")
  private LocalDateTime expiresAt; // 구독 만료일

  @Column(name = "payment_provider")
  private String paymentProvider; // 결제 수단/제공자 (TOSS 등)

  @Column(name = "billing_key")
  private String billingKey; // PG(토스페이먼츠)에서 발급받은 빌링키 - 카드 최초 등록 후 저장, 이후 정기결제에 사용

  @Column(name = "customer_key")
  private String customerKey; // 빌링키 발급 시 사용한 토스 customerKey - 결제 승인 요청 시 반드시 동일한 값을 써야 해서 같이 저장

  @Builder.Default
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now(); // 생성 일시

}
