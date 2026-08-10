package org.ict.datemanagerbackend.domain.calendar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ict.datemanagerbackend.domain.couple.entity.Couple;
import java.time.LocalDate;
import java.time.LocalDateTime;

// title/anniversaryDate/alarmType은 생성 이후 사용자가 수정할 수 있어 개별 setter를 열어둔다.
@Entity
@Table(
    name = "couple_anniversaries",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_couple_anniversaries", columnNames = {"couple_id", "anniversary_date", "title"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class CoupleAnniversary {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 기념일 ID (PK)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "couple_id", nullable = false)
  private Couple couple; // 커플 (couples.id 참조)

  @Setter
  @Column(name = "title", nullable = false, length = 100)
  private String title; // 기념일 명칭

  @Setter
  @Column(name = "anniversary_date", nullable = false)
  private LocalDate anniversaryDate; // 기념일 날짜

  @Setter
  @Column(name = "alarm_type", insertable = false, length = 30)
  private String alarmType; // 알림 방식 (생성 시 DB 기본값 'D_DAY', 이후 사용자가 변경 가능)

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt; // 생성 일시

}
