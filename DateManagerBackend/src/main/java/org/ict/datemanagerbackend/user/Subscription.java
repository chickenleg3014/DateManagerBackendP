package org.ict.datemanagerbackend.user;

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

  @Column(name = "user_id", nullable = false)
  private Long userId; // 회원 ID (users.id 참조)

  @Column(name = "plan_code", nullable = false)
  private String planCode; // 구독 플랜 코드 (FREE, PREMIUM_MONTHLY 등)

  @Column(nullable = false, insertable = false, updatable = false)
  private String status; // 구독 상태 (ACTIVE, CANCELED, EXPIRED - DB 기본값 활용)

  @Column(name = "started_at", insertable = false, updatable = false)
  private LocalDateTime startedAt; // 구독 시작일

  @Column(name = "expires_at")
  private LocalDateTime expiresAt; // 구독 만료일

  @Column(name = "payment_provider")
  private String paymentProvider; // 결제 수단/제공자 (IAP, PG 등)

  @Column(name = "created_at", insertable = false, updatable = false)
  private LocalDateTime createdAt; // 생성 일시

}
