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
import lombok.Setter;

@Entity
@Table(
    name = "place_amenities",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_place_amenities", columnNames = {"place_id", "amenity_tag"})
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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
