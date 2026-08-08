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

@Entity
@Table(name = "place_realities")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PlaceReality {

  @Id
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "place_id")
  private Place place; // 장소와 PK를 공유하는 1:1 관계

  @Column(name = "waiting_status", nullable = false, insertable = false, updatable = false)
  private String waitingStatus; // 웨이팅 상태 (NONE, WAITING 등 - DB 기본값 활용)

  @Column(name = "waiting_teams", nullable = false, insertable = false, updatable = false)
  private Integer waitingTeams; // 현재 대기 팀 수 (DB 기본값 활용)

  @Column(name = "reservation_type", nullable = false, insertable = false, updatable = false)
  private String reservationType; // 예약 방식 (REQUIRED, WALKIN 등 - DB 기본값 활용)

  @Column(name = "price_text", length = 50)
  private String priceText; // 가격대 안내 태그

  @Column(name = "parking_info", length = 100)
  private String parkingInfo; // 주차 정보

  @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime updatedAt; // 수정 일시

}
