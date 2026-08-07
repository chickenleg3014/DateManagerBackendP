package org.ict.datemanagerbackend.domain.course.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_groups")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CourseGroup {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 코스 그룹 ID (PK)

  @Column(name = "couple_id")
  private Long coupleId; // 커플 ID (couples.id 참조)

  @Column(name = "creator_user_id", nullable = false)
  private Long creatorUserId; // 생성 유저 ID (users.id 참조)

  @Column(name = "title", nullable = false, length = 100)
  private String title; // 코스 타이틀

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt; // 생성 일시

}