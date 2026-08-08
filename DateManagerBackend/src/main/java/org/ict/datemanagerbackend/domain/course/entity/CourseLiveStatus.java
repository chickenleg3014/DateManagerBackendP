package org.ict.datemanagerbackend.domain.course.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_live_status")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CourseLiveStatus {

  @Id
  @Column(name = "course_group_id")
  private Long courseGroupId;

  @Column(name = "is_live_active", nullable = false, insertable = false, updatable = false)
  private Integer isLiveActive;

  @Column(name = "current_mood_mode", nullable = false)
  private String currentMoodMode;

  @Column(name = "last_latitude")
  private Double lastLatitude;

  @Column(name = "last_longitude")
  private Double lastLongitude;

  @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime updatedAt;
}