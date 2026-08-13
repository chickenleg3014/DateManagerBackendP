package org.ict.datemanagerbackend.domain.couple.entity;

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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * "커플 관계" 그 자체를 나타내는 최상위 엔티티. 실제로 누가 그 커플에 속해있는지는
 * 이 테이블이 아니라 {@link CoupleMember}(중간 테이블)가 담당한다 — 즉 Couple 자체는
 * "관계라는 개념 하나"만 표현하고, 그 관계에 누가 참여하는지는 별도로 관리하는 구조다.
 * (이렇게 분리해두면 나중에 "3인 이상 그룹"처럼 확장할 일이 생겨도 Couple 테이블 구조를
 *  안 건드리고 CoupleMember 쪽만 조정하면 된다.)
 *
 * <p>인스턴스 생성 경로는 {@code org.ict.datemanagerbackend.domain.couple.controller.CoupleController}의
 * acceptInvite() 하나뿐이다 — 즉 초대 링크가 수락되는 순간에만 새 Couple이 만들어진다.
 */
// 주의: couples 테이블은 수동 DDL이 아니라 JPA ddl-auto로만 생성된 테이블이라 DB 레벨 DEFAULT가 없다
// (Place.java의 createdAt과 동일한 이유). 그래서 예전 주석에 적혀있던 "DB 기본값을 그대로 쓴다"는
// 전제가 틀렸었다 - status/connectedAt 모두 insertable=false로 두면 INSERT 문에서 컬럼 자체가 빠지는데
// DB에 채워줄 기본값이 없으니 NOT NULL 제약(status)에 걸려 ORA-01400이 난다. 그래서 두 필드 다
// @Builder.Default로 애플리케이션이 직접 값을 채우도록 바꿨다.
// status는 생성 이후 관리자가 강제로 연결 해제(DISCONNECTED)하거나 사용자가 직접 연결을 끊을 수 있어야 해서
// setter를 열어둔다 - "처음 만들 땐 ACTIVE로 고정, 나중엔 바꿀 수 있다"는 뜻.
@Entity
@Table(name = "couples")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA가 리플렉션으로 객체를 만들 때 쓰는 기본 생성자(외부에서 직접 new 금지)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class Couple {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) // Oracle의 IDENTITY 컬럼(자동 증가 PK)을 그대로 사용
  private Long id; // 커플 그룹 ID (PK)

  @Setter
  @Builder.Default
  @Column(nullable = false)
  private String status = "ACTIVE"; // 커플 상태 (ACTIVE, DISCONNECTED - 생성 시 ACTIVE로 시작, 이후 setStatus()로 변경 가능)

  @Builder.Default
  @Column(name = "connected_at", updatable = false)
  private LocalDateTime connectedAt = LocalDateTime.now(); // 연동(커플 성사) 일시 - Couple이 만들어지는 순간의 시각

  // 커플이 "실제로 처음 만난 날짜" - connectedAt(이 사이트에서 연결된 시각)과는 별개의 값이다.
  // Couple 생성 시점엔 알 수 없고(초대 수락만으로는 만난 날짜를 알 방법이 없음) 나중에
  // 커플싱크 탭에서 두 사람 중 누구든 직접 입력/수정할 수 있어야 해서 nullable + setter를 둔다.
  @Setter
  @Column(name = "met_date")
  private LocalDate metDate;

}
