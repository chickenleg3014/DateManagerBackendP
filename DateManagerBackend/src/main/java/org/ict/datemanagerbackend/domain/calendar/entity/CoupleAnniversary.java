package org.ict.datemanagerbackend.domain.calendar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "couple_anniversaries",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_couple_anniversaries", columnNames = {"couple_id", "anniversary_date", "title"})
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CoupleAnniversary {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 기념일 ID (PK)

  @Column(name = "couple_id", nullable = false)
  private Long coupleId; // 커플 ID (couples.id 참조)

  @Column(name = "title", nullable = false, length = 100)
  private String title; // 기념일 명칭

  @Column(name = "anniversary_date", nullable = false)
  private LocalDate anniversaryDate; // 기념일 날짜

  @Column(name = "alarm_type", insertable = false, updatable = false, length = 30)
  private String alarmType; // 알림 방식 (DB 기본값 'D_DAY' 활용)

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt; // 생성 일시

}