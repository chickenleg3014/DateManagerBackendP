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

import java.time.LocalDateTime;

// status/connectedAt 모두 DB 기본값이 채우는 값(insertable/updatable=false)이라 setter가 없다.
// status를 앱에서 직접 바꿔야 한다면(예: 연결 해제) insertable/updatable 제약부터 별도로 풀어야 한다.
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

  @Column(nullable = false, insertable = false, updatable = false)
  private String status; // 커플 상태 (ACTIVE, DISCONNECTED - DB 기본값 활용)

  @Column(name = "connected_at", insertable = false, updatable = false)
  private LocalDateTime connectedAt; // 연동 일시

}
