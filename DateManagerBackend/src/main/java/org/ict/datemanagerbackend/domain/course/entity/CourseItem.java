package org.ict.datemanagerbackend.domain.course.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ict.datemanagerbackend.domain.place.entity.Place;

// place/sequence/transitTimeMinutes/stayTimeMinutes는 재라우팅(CourseReRouting)이나 코스 재편집 시
// 갱신될 수 있어 개별 setter를 열어둔다. courseGroup은 소속 관계라 불변.
@Entity
@Table(name = "course_items", uniqueConstraints = {
    @UniqueConstraint(name = "uq_course_items", columnNames = {"course_group_id", "sequence"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class CourseItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_group_id", nullable = false)
  private CourseGroup courseGroup;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "place_id", nullable = false)
  private Place place;

  @Setter
  @Column(name = "sequence", nullable = false)
  private Integer sequence;

  @Setter
  @Column(name = "transit_time_minutes")
  private Integer transitTimeMinutes;

  @Setter
  @Column(name = "stay_time_minutes")
  private Integer stayTimeMinutes;
}
