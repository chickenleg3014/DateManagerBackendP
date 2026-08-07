package org.ict.datemanagerbackend.domain.course.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_live_logs")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CourseLiveLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "course_group_id", nullable = false)
  private Long courseGroupId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "switched_mood_mode", nullable = false)
  private String switchedMoodMode;

  @Column(name = "latitude_at_click")
  private Double latitudeAtClick;

  @Column(name = "longitude_at_click")
  private Double longitudeAtClick;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt;
}