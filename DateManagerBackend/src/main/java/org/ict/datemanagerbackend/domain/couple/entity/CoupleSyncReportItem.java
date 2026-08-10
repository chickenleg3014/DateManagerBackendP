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

// 영수증 콘셉트 리포트의 하위 항목이라 생성 이후 값이 바뀌지 않아 setter가 없다.
@Entity
@Table(
    name = "couple_sync_report_items",
    uniqueConstraints = {
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
  private CoupleSyncReport report; // 리포트 (couple_sync_reports.id 참조)

  @Column(name = "rank_no", nullable = false)
  private Integer rankNo; // 추천 순위 (1위, 2위 등)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "place_id", nullable = false)
  private Place place; // 장소 (places.id 참조)

  @Lob
  @Column(name = "reason_text")
  private String reasonText; // 추천 사유 텍스트 (CLOB 매핑)

}
