package org.ict.datemanagerbackend.domain.place.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 태그 자체가 바뀌는 경우는 없고(바꾸려면 삭제 후 재추가) 생성 이후 불변이라 setter가 없다.
@Entity
@Table(
    name = "place_amenities",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_place_amenities", columnNames = {"place_id", "amenity_tag"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class PlaceAmenity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 편의시설 ID (PK)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "place_id", nullable = false)
  private Place place; // 장소 (places.id 참조)

  @Column(name = "amenity_tag", nullable = false, length = 100)
  private String amenityTag; // 편의시설 태그 (WIFI, PET 등)

}
