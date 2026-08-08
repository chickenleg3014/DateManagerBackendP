package org.ict.datemanagerbackend.domain.calendar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_calendars")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserCalendar {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 커플 데이트 캘린더 일정 ID (PK)

  @Column(name = "user_id", nullable = false)
  private Long userId; // 일정을 소유한 회원 ID (users.id 참조)

  @Column(name = "title", nullable = false, length = 100)
  private String title; // 일정 제목

  @Lob
  @Column(name = "description")
  private String description; // 상세 일정 메모 (CLOB 매핑)

  @Column(name = "target_date", nullable = false)
  private LocalDate targetDate; // 일정 약속 날짜

  @Column(name = "course_group_id")
  private Long courseGroupId; // 연결된 데이트 코스 ID (course_groups.id 참조)

  @Column(name = "created_at", insertable = false, updatable = false)
  private LocalDateTime createdAt; // 생성 일시 (DB 기본값 활용)

}
