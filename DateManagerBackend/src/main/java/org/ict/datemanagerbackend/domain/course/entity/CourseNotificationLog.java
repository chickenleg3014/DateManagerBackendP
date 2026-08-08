package org.ict.datemanagerbackend.domain.course.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_notification_logs")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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

  @Column(name = "is_clicked", nullable = false, insertable = false, updatable = false)
  private Integer isClicked;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt;
}
