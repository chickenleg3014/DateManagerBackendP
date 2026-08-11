package org.ict.datemanagerbackend.domain.couple.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ict.datemanagerbackend.domain.place.entity.Place;

/**
 * ⚠️ 아직 Repository/Service/Controller가 없는 "엔티티만 먼저 만들어둔" 상태(2026-08-11 기준).
 * {@link CoupleSyncReport}(리포트 한 장) 안에 들어가는 "추천 장소 한 줄"에 해당하는 하위 항목.
 * 리포트 하나가 장소를 여러 개(1위, 2위, 3위...) 추천할 수 있으므로 1:N 관계로 설계되어 있다.
 *
 * <p>이미 완성되어 있는 {@link org.ict.datemanagerbackend.domain.place.entity.Place} 도메인
 * (KOPIS/TourAPI로 동기화된 실제 장소 데이터, {@link org.ict.datemanagerbackend.domain.place.controller.PlaceController}
 * 참고)을 그대로 참조하도록 설계되어 있어서, 밸런싱 로직을 실제로 구현할 때
 * place 테이블에 이미 쌓인 데이터를 곧바로 추천 후보로 활용할 수 있는 구조다.
 */
// 영수증 리포트의 하위 항목이라 생성 이후 값이 바뀌지 않아 setter가 없다(불변 데이터).
@Entity
@Table(
    name = "couple_sync_report_items",
    uniqueConstraints = {
        // 한 리포트 안에서 같은 순위(rank_no)가 중복되지 않도록 보장 (1위가 두 개일 수 없음)
        @UniqueConstraint(name = "uq_couple_sync_report_items", columnNames = {"report_id", "rank_no"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class CoupleSyncReportItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 리포트 아이템 ID (PK)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "report_id", nullable = false)
  private CoupleSyncReport report; // 이 항목이 속한 리포트 (couple_sync_reports.id 참조)

  @Column(name = "rank_no", nullable = false)
  private Integer rankNo; // 추천 순위 (1위, 2위 등) - 값이 작을수록 우선 추천

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "place_id", nullable = false)
  private Place place; // 추천 장소 (places.id 참조) - KOPIS/TourAPI 동기화 데이터를 그대로 활용

  // @Lob: Oracle CLOB과 매핑되는 긴 텍스트 컬럼(길이 제한 없음)
  @Lob
  @Column(name = "reason_text")
  private String reasonText; // 이 장소를 추천한 이유 텍스트 (예: "두 분 모두 실내 활동을 선호해서 추천해요")

}
