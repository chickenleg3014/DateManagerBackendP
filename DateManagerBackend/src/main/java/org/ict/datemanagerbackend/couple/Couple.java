package org.ict.datemanagerbackend.couple;

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
@Table(name = "couples")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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
