package org.ict.datemanagerbackend.domain.course.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ict.datemanagerbackend.domain.couple.entity.Couple;
import org.ict.datemanagerbackend.domain.user.entity.User;

import java.time.LocalDateTime;

// title만 생성 이후 이름 변경이 가능해 setter를 열어둔다. couple/creator는 소속 관계라 불변.
@Entity
@Table(name = "course_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class CourseGroup {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 코스 그룹 ID (PK)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "couple_id")
  private Couple couple; // 커플 (couples.id 참조, 솔로 모드면 null)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "creator_user_id", nullable = false)
  private User creator; // 생성 유저 (users.id 참조)

  @Setter
  @Column(name = "title", nullable = false, length = 100)
  private String title; // 코스 타이틀

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt; // 생성 일시

}
