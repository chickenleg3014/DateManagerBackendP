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

// connectedAt은 DB 기본값이 채우는 값이라 setter가 없다.
// status는 생성 시엔 DB 기본값(ACTIVE)을 그대로 쓰지만(insertable=false 유지),
// 관리자가 강제로 연결 해제(DISCONNECTED)할 수 있어야 해서 updatable만 풀어 setter를 열어둔다.
@Entity
@Table(name = "couples")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class Couple {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 커플 그룹 ID (PK)

  @Setter
  @Column(nullable = false, insertable = false)
  private String status; // 커플 상태 (ACTIVE, DISCONNECTED - 생성 시엔 DB 기본값, 이후 관리자가 변경 가능)

  @Column(name = "connected_at", insertable = false, updatable = false)
  private LocalDateTime connectedAt; // 연동 일시

}
