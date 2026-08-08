package org.ict.datemanagerbackend.domain.course.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_items", uniqueConstraints = {
    @UniqueConstraint(name = "uq_course_items", columnNames = {"course_group_id", "sequence"})
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CourseItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "course_group_id", nullable = false)
  private Long courseGroupId;

  @Column(name = "place_id", nullable = false)
  private Long placeId;

  @Column(name = "sequence", nullable = false)
  private Integer sequence;

  @Column(name = "transit_time_minutes")
  private Integer transitTimeMinutes;

  @Column(name = "stay_time_minutes")
  private Integer stayTimeMinutes;
}