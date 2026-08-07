package org.ict.datemanagerbackend.domain.couple.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
    name = "couple_sync_report_items",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_couple_sync_report_items", columnNames = {"report_id", "rank_no"})
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CoupleSyncReportItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 리포트 아이템 ID (PK)

  @Column(name = "report_id", nullable = false)
  private Long reportId; // 리포트 ID (couple_sync_reports.id 참조)

  @Column(name = "rank_no", nullable = false)
  private Integer rankNo; // 추천 순위 (1위, 2위 등)

  @Column(name = "place_id", nullable = false)
  private Long placeId; // 장소 ID (places.id 참조)

  @Lob
  @Column(name = "reason_text")
  private String reasonText; // 추천 사유 텍스트 (CLOB 매핑)

}