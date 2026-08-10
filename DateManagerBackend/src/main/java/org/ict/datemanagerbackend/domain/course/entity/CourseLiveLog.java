package org.ict.datemanagerbackend.domain.course.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ict.datemanagerbackend.domain.user.entity.User;
import java.time.LocalDateTime;

// 클릭 시점 스냅샷 로그라 생성 이후 값이 바뀌지 않아 setter가 없다.
@Entity
@Table(name = "course_live_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class CourseLiveLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_group_id", nullable = false)
  private CourseGroup courseGroup;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "switched_mood_mode", nullable = false)
  private String switchedMoodMode;

  @Column(name = "latitude_at_click")
  private Double latitudeAtClick;

  @Column(name = "longitude_at_click")
  private Double longitudeAtClick;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt;
}
