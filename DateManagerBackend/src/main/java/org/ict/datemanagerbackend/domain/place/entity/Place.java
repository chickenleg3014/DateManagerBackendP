package org.ict.datemanagerbackend.domain.place.entity;

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
@Table(name = "places")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Place {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 장소 ID (PK)

  @Column(name = "name", nullable = false, length = 100)
  private String name; // 장소명

  @Column(name = "category", length = 50)
  private String category; // 카테고리 (카페, 맛집 등)

  @Column(name = "address", length = 255)
  private String address; // 도로명 주소

  @Column(name = "latitude")
  private Double latitude; // 위도

  @Column(name = "longitude")
  private Double longitude; // 경도

  @Column(name = "image_url", length = 500)
  private String imageUrl; // 썸네일 URL

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt; // 생성 일시

}