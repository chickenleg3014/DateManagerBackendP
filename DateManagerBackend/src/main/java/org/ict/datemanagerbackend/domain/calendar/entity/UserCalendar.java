package org.ict.datemanagerbackend.domain.calendar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ict.datemanagerbackend.domain.course.entity.CourseGroup;
import org.ict.datemanagerbackend.domain.user.entity.User;
import java.time.LocalDate;
import java.time.LocalDateTime;

// title/description/targetDate/courseGroup은 생성 이후 사용자가 일정을 수정할 수 있어 개별 setter를 열어둔다.
@Entity
@Table(name = "user_calendars")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class UserCalendar {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 커플 데이트 캘린더 일정 ID (PK)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 일정을 소유한 회원 (users.id 참조)

  @Setter
  @Column(name = "title", nullable = false, length = 100)
  private String title; // 일정 제목

  @Setter
  @Lob
  @Column(name = "description")
  private String description; // 상세 일정 메모 (CLOB 매핑)

  @Setter
  @Column(name = "target_date", nullable = false)
  private LocalDate targetDate; // 일정 약속 날짜

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_group_id")
  private CourseGroup courseGroup; // 연결된 데이트 코스 (course_groups.id 참조)

  @Column(name = "created_at", insertable = false, updatable = false)
  private LocalDateTime createdAt; // 생성 일시 (DB 기본값 활용)

}
