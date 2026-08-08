package org.ict.datemanagerbackend.domain.place.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "place_styles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PlaceStyle {

  @Id
  @Column(name = "place_id")
  private Long placeId; // 장소 ID (1:1 관계, places.id 참조 / PK)

  @Column(name = "score_energy", nullable = false, insertable = false, updatable = false)
  private Integer scoreEnergy; // 에너지 성향 점수 (DB 기본값 활용)

  @Column(name = "score_immersion", nullable = false, insertable = false, updatable = false)
  private Integer scoreImmersion; // 몰입 성향 점수 (DB 기본값 활용)

  @Column(name = "score_vibe", nullable = false, insertable = false, updatable = false)
  private Integer scoreVibe; // 분위기 성향 점수 (DB 기본값 활용)

  @Column(name = "score_aesthetic", nullable = false, insertable = false, updatable = false)
  private Integer scoreAesthetic; // 미감 성향 점수 (DB 기본값 활용)

  @Column(name = "score_depth", nullable = false, insertable = false, updatable = false)
  private Integer scoreDepth; // 깊이 성향 점수 (DB 기본값 활용)

  @Column(name = "is_indoor", nullable = false, insertable = false, updatable = false)
  private Integer isIndoor; // 실내 여부 (0/1 - DB 기본값 활용)

  @Column(name = "is_activity", nullable = false, insertable = false, updatable = false)
  private Integer isActivity; // 액티비티 여부 (0/1 - DB 기본값 활용)

  @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime updatedAt; // 수정 일시

}