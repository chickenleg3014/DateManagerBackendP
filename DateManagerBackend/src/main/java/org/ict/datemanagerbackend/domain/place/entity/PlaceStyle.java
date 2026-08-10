package org.ict.datemanagerbackend.domain.place.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// score_*/is_indoor/is_activity는 큐레이션 로직이 생성 이후 계속 갱신하는 값이라 개별 setter를 열어둔다.
@Entity
@Table(name = "place_styles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class PlaceStyle {

  @Id
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "place_id")
  private Place place; // 장소와 PK를 공유하는 1:1 관계

  @Setter
  @Column(name = "score_energy", nullable = false, insertable = false)
  private Integer scoreEnergy; // 에너지 성향 점수 (생성 시 DB 기본값 50, 이후 큐레이션 로직이 갱신)

  @Setter
  @Column(name = "score_immersion", nullable = false, insertable = false)
  private Integer scoreImmersion; // 몰입 성향 점수 (생성 시 DB 기본값 50, 이후 큐레이션 로직이 갱신)

  @Setter
  @Column(name = "score_vibe", nullable = false, insertable = false)
  private Integer scoreVibe; // 분위기 성향 점수 (생성 시 DB 기본값 50, 이후 큐레이션 로직이 갱신)

  @Setter
  @Column(name = "score_aesthetic", nullable = false, insertable = false)
  private Integer scoreAesthetic; // 미감 성향 점수 (생성 시 DB 기본값 50, 이후 큐레이션 로직이 갱신)

  @Setter
  @Column(name = "score_depth", nullable = false, insertable = false)
  private Integer scoreDepth; // 깊이 성향 점수 (생성 시 DB 기본값 50, 이후 큐레이션 로직이 갱신)

  @Setter
  @Column(name = "is_indoor", nullable = false, insertable = false)
  private Integer isIndoor; // 실내 여부 (0/1, 생성 시 DB 기본값 1, 이후 큐레이션 로직이 갱신)

  @Setter
  @Column(name = "is_activity", nullable = false, insertable = false)
  private Integer isActivity; // 액티비티 여부 (0/1, 생성 시 DB 기본값 0, 이후 큐레이션 로직이 갱신)

  @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime updatedAt; // 수정 일시

}
