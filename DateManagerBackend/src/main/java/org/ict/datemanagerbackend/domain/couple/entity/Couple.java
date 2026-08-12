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
// connectedAt은 DB 기본값(SYSTIMESTAMP)이 채우는 값이라 자바 코드에서 직접 넣을 수 없어(insertable=false,
// updatable=false) setter가 없다.
// status는 생성 시엔 DB 기본값(ACTIVE)을 그대로 쓰지만(그래서 insertable=false는 유지),
// 관리자가 강제로 연결 해제(DISCONNECTED)하거나 사용자가 직접 연결을 끊을 수 있어야 해서
// updatable은 막지 않고 setter를 열어둔다 — "처음 만들 때는 내가 못 정하지만, 나중엔 바꿀 수 있다"는 뜻.
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
  @Column(nullable = false, insertable = false)
  private String status; // 커플 상태 (ACTIVE, DISCONNECTED - 생성 시엔 DB 기본값, 이후 setStatus()로 변경 가능)

  @Column(name = "connected_at", insertable = false, updatable = false)
  private LocalDateTime connectedAt; // 연동(커플 성사) 일시 - DB가 SYSTIMESTAMP로 자동 기록

}
