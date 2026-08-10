package org.ict.datemanagerbackend.domain.course.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// isClicked만 클릭 발생 시 갱신되는 값이라 setter를 열어둔다. 나머지는 발송 시점에 고정되는 로그 내용.
@Entity
@Table(name = "course_notification_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class CourseNotificationLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_group_id", nullable = false)
  private CourseGroup courseGroup;

  @Column(name = "notification_type", nullable = false)
  private String notificationType;

  @Lob
  @Column(name = "message_text")
  private String messageText;

  @Setter
  @Column(name = "is_clicked", nullable = false, insertable = false)
  private Integer isClicked; // 생성 시 DB 기본값 0, 클릭 시 앱이 1로 갱신

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt;
}
