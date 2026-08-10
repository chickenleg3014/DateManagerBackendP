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

// waitingStatus/waitingTeams/reservationType/priceText/parkingInfo는 실시간 동기화·정보 갱신으로
// 계속 바뀌는 값이라 개별 setter를 열어둔다. place는 1:1 PK 공유라 불변.
@Entity
@Table(name = "place_realities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class PlaceReality {

  @Id
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "place_id")
  private Place place; // 장소와 PK를 공유하는 1:1 관계

  @Setter
  @Column(name = "waiting_status", nullable = false, insertable = false)
  private String waitingStatus; // 웨이팅 상태 (생성 시 DB 기본값 'NONE', 이후 실시간 동기화 로직이 갱신)

  @Setter
  @Column(name = "waiting_teams", nullable = false, insertable = false)
  private Integer waitingTeams; // 현재 대기 팀 수 (생성 시 DB 기본값 0, 이후 실시간 동기화 로직이 갱신)

  @Setter
  @Column(name = "reservation_type", nullable = false, insertable = false)
  private String reservationType; // 예약 방식 (생성 시 DB 기본값 'WALKIN', 이후 실시간 동기화 로직이 갱신)

  @Setter
  @Column(name = "price_text", length = 50)
  private String priceText; // 가격대 안내 태그

  @Setter
  @Column(name = "parking_info", length = 100)
  private String parkingInfo; // 주차 정보

  @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime updatedAt; // 수정 일시

}
