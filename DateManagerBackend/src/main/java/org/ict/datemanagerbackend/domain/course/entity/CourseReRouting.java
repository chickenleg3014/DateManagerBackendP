package org.ict.datemanagerbackend.domain.course.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_re_routings")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CourseReRouting {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "course_group_id", nullable = false)
  private Long courseGroupId;

  @Column(name = "sequence", nullable = false)
  private Integer sequence;

  @Column(name = "original_place_id", nullable = false)
  private Long originalPlaceId;

  @Column(name = "replaced_place_id", nullable = false)
  private Long replacedPlaceId;

  @Column(name = "trigger_type", nullable = false)
  private String triggerType;

  @Column(name = "is_applied", nullable = false, insertable = false, updatable = false)
  private Integer isApplied;

  @Column(name = "routing_reason_text")
  private String routingReasonText;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt;
}